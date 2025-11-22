package com.example.wetherapp.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wetherapp.data.api.RetrofitClient
import com.example.wetherapp.data.model.WeatherResponse
import com.example.wetherapp.data.model.ForecastResponse
import com.example.wetherapp.data.repository.WeatherRepository
import kotlinx.coroutines.launch

class WeatherViewModel : ViewModel() {

    private val repository = WeatherRepository(RetrofitClient.api)

    private val _weatherResult = MutableLiveData<Result<WeatherResponse>>()
    val weatherResult: LiveData<Result<WeatherResponse>> = _weatherResult

    private val _forecastResult = MutableLiveData<Result<ForecastResponse>>()
    val forecastResult: LiveData<Result<ForecastResponse>> = _forecastResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private var lastCity: String? = null
    private var lastLocation: Pair<Double, Double>? = null

    fun refreshWeather(apiKey: String) {
        lastCity?.let {
            getWeatherByCity(it, apiKey)
        } ?: lastLocation?.let {
            getWeatherByLocation(it.first, it.second, apiKey)
        }
    }

    fun getWeatherByCity(city: String, apiKey: String) {
        lastCity = city
        lastLocation = null
        Log.d("WeatherViewModel", "Fetching weather for city: $city with key: $apiKey")
        val lang = java.util.Locale.getDefault().language
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val weatherResult = repository.getCurrentWeatherByCity(city, apiKey, lang)
                _weatherResult.value = weatherResult
                
                if (weatherResult.isSuccess) {
                    val forecastResult = repository.getForecast(city, apiKey, lang)
                    val filteredForecast = forecastResult.map { response ->
                        response.copy(list = filterForecastByDay(response.list))
                    }
                    _forecastResult.value = filteredForecast
                }
            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Exception: ${e.message}", e)
                _weatherResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getWeatherByLocation(lat: Double, lon: Double, apiKey: String) {
        lastLocation = lat to lon
        lastCity = null
        val lang = java.util.Locale.getDefault().language
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val weatherResult = repository.getCurrentWeatherByLocation(lat, lon, apiKey, lang)
                _weatherResult.value = weatherResult
                
                if (weatherResult.isSuccess) {
                    val city = weatherResult.getOrNull()?.name ?: ""
                    if (city.isNotEmpty()) {
                        val forecastResult = repository.getForecast(city, apiKey, lang)
                        val filteredForecast = forecastResult.map { response ->
                            response.copy(list = filterForecastByDay(response.list))
                        }
                        _forecastResult.value = filteredForecast
                    }
                }
            } catch (e: Exception) {
                _weatherResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun filterForecastByDay(list: List<com.example.wetherapp.data.model.ForecastItem>): List<com.example.wetherapp.data.model.ForecastItem> {
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        
        // Group by day, exclude today
        return list.filter { !it.dt_txt.startsWith(today) }
            .groupBy { it.dt_txt.substring(0, 10) }
            .map { (_, items) ->
                // 1. Find the maximum temperature of the day
                val maxTemp = items.maxOf { it.main.temp }
                
                // 2. Pick the weather condition at noon (12:00) as representative
                val noonItem = items.minByOrNull { kotlin.math.abs(it.dt_txt.substring(11, 13).toInt() - 12) } ?: items.first()
                
                // 3. Create a new item with Max Temp and Noon Weather
                noonItem.copy(
                    main = noonItem.main.copy(temp = maxTemp)
                )
            }
            .take(5)
    }
}
