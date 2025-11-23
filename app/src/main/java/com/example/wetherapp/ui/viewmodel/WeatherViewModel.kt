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
                    _forecastResult.value = forecastResult
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
                        _forecastResult.value = forecastResult
                    }
                }
            } catch (e: Exception) {
                _weatherResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
