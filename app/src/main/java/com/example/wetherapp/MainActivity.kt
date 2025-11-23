package com.example.wetherapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import coil.load
import com.example.wetherapp.databinding.ActivityMainBinding
import com.example.wetherapp.ui.adapter.ForecastAdapter
import com.example.wetherapp.ui.HourlyForecastAdapter
import com.example.wetherapp.ui.viewmodel.WeatherViewModel
import com.example.wetherapp.data.model.ForecastResponse
import com.example.wetherapp.data.model.ForecastItem
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: WeatherViewModel by viewModels()
    private lateinit var forecastAdapter: ForecastAdapter
    private lateinit var hourlyForecastAdapter: HourlyForecastAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    
    // Store full forecast data to filter for hourly view
    private var fullForecastList: List<ForecastItem> = emptyList()

    // API Key is now loaded from local.properties via BuildConfig
    private val apiKey = BuildConfig.API_KEY

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                getCurrentLocation()
            } else {
                Toast.makeText(this, getString(R.string.location_denied), Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        
        // Initialize Adapters
        hourlyForecastAdapter = HourlyForecastAdapter(emptyList())
        binding.rvHourlyForecast.adapter = hourlyForecastAdapter
        
        forecastAdapter = ForecastAdapter { selectedDay ->
            showDayHourly(selectedDay)
        }
        binding.rvForecast.adapter = forecastAdapter

        setupListeners()
        observeViewModel()
        
        if (viewModel.weatherResult.value != null) {
            viewModel.refreshWeather(apiKey)
        } else {
            checkLocationPermission()
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getCurrentLocation()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val geocoder = android.location.Geocoder(this, java.util.Locale.getDefault())
                    
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        geocoder.getFromLocation(location.latitude, location.longitude, 1) { addresses ->
                            val address = addresses.firstOrNull()
                            // Prefer AdminArea (Province) over Locality (City/District)
                            val city = address?.adminArea ?: address?.locality
                            
                            runOnUiThread {
                                if (!city.isNullOrEmpty()) {
                                    viewModel.getWeatherByCity(city, apiKey)
                                } else {
                                    viewModel.getWeatherByLocation(location.latitude, location.longitude, apiKey)
                                }
                            }
                        }
                    } else {
                        Thread {
                            try {
                                @Suppress("DEPRECATION")
                                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                                val address = addresses?.firstOrNull()
                                val city = address?.adminArea ?: address?.locality
                                
                                runOnUiThread {
                                    if (!city.isNullOrEmpty()) {
                                        viewModel.getWeatherByCity(city, apiKey)
                                    } else {
                                        viewModel.getWeatherByLocation(location.latitude, location.longitude, apiKey)
                                    }
                                }
                            } catch (e: Exception) {
                                runOnUiThread {
                                    viewModel.getWeatherByLocation(location.latitude, location.longitude, apiKey)
                                }
                            }
                        }.start()
                    }
                } else {
                    // If location data is not available, use Istanbul as default city
                    viewModel.getWeatherByCity("Istanbul", apiKey)
                }
            }
            .addOnFailureListener {
                // Failed to get location, use Istanbul
                viewModel.getWeatherByCity("Istanbul", apiKey)
            }
    }

    private val cities = com.example.wetherapp.data.CityData.cities

    private fun setupListeners() {
        binding.btnSearch.setOnClickListener {
            handleSearch()
        }

        binding.etCity.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                handleSearch()
                true
            } else {
                false
            }
        }

        binding.btnLanguage.setOnClickListener {
            toggleLanguage()
        }
        
        // Reset to current hourly forecast when main card is clicked
        binding.weatherCard.setOnClickListener {
            showCurrentHourly()
        }
    }

    private fun toggleLanguage() {
        val currentLang = resources.configuration.locales[0].language
        val newLang = if (currentLang == "tr") "en" else "tr"
        
        val locale = java.util.Locale(newLang)
        java.util.Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)
        
        // Update UI text manually to avoid activity restart (which causes crashes/bad UX)
        updateLocalizedText()
        
        // Refresh weather data with new language
        viewModel.refreshWeather(apiKey)
    }

    private fun updateLocalizedText() {
        binding.tvAppTitle.text = getString(R.string.app_title)
        binding.btnLanguage.text = getString(R.string.lang_toggle)
        binding.etCity.hint = getString(R.string.search_hint)
        binding.tvForecastTitle.text = getString(R.string.next_5_days)
        
        // Update static labels
        binding.labelHumidity.text = getString(R.string.humidity)
        binding.labelWind.text = getString(R.string.wind)
        binding.labelFeelsLike.text = getString(R.string.feels_like)
    }

    private fun handleSearch() {
        val inputCity = binding.etCity.text.toString().trim()
        if (inputCity.isNotEmpty()) {
            val closestCity = findClosestCity(inputCity)
            if (closestCity != null && !closestCity.equals(inputCity, ignoreCase = true)) {
                viewModel.getWeatherByCity(closestCity, apiKey)
                binding.etCity.setText(closestCity)
            } else {
                viewModel.getWeatherByCity(inputCity, apiKey)
            }
            
            // Clear search bar and hide keyboard
            binding.etCity.text.clear()
            val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(binding.etCity.windowToken, 0)
            binding.etCity.clearFocus()
        } else {
            android.util.Log.d("MainActivity", "City is empty")
        }
    }

    private fun findClosestCity(input: String): String? {
        // 1. Check for exact match (case-insensitive)
        val exactMatch = cities.find { it.equals(input, ignoreCase = true) }
        if (exactMatch != null) return exactMatch

        // 2. Find closest match using Levenshtein distance
        var closestCity: String? = null
        var minDistance = Int.MAX_VALUE

        for (city in cities) {
            val distance = levenshteinDistance(input.lowercase(), city.lowercase())
            if (distance < minDistance) {
                minDistance = distance
                closestCity = city
            }
        }

        // Only return if the match is "close enough" (e.g., distance <= 3)
        // Adjust threshold based on word length if needed
        return if (minDistance <= 3) closestCity else null
    }

    private fun levenshteinDistance(s1: String, s2: String): Int {
        val m = s1.length
        val n = s2.length
        val dp = Array(m + 1) { IntArray(n + 1) }

        for (i in 0..m) dp[i][0] = i
        for (j in 0..n) dp[0][j] = j

        for (i in 1..m) {
            for (j in 1..n) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,      // Deletion
                    dp[i][j - 1] + 1,      // Insertion
                    dp[i - 1][j - 1] + cost // Substitution
                )
            }
        }
        return dp[m][n]
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.tvError.visibility = View.GONE
        }

        viewModel.weatherResult.observe(this) { result ->
            result?.onSuccess { weather ->
                binding.tvCityName.text = weather.name.replace(" Province", "").replace(" City", "")
                binding.tvTemperature.text = "${weather.main.temp.toInt()}°"
                binding.tvDescription.text = weather.weather.firstOrNull()?.description?.replaceFirstChar { it.uppercase() } ?: ""
                
                // Detailed Stats
                binding.tvHumidity.text = "${weather.main.humidity}%"
                binding.tvWind.text = "${weather.wind.speed} km/h"
                binding.tvFeelsLike.text = "${weather.main.feelsLike.toInt()}°"
                
                val iconCode = weather.weather.firstOrNull()?.icon
                if (iconCode != null) {
                    val iconUrl = "http://openweathermap.org/img/wn/$iconCode@4x.png"
                    binding.ivWeatherIcon.load(iconUrl) {
                        crossfade(true)
                    }
                    updateBackground(weather.weather.firstOrNull()?.main)
                }
            }?.onFailure { exception ->
                binding.tvError.text = "${getString(R.string.error_prefix)}${exception.message}"
                binding.tvError.visibility = View.VISIBLE
            }
        }

        viewModel.forecastResult.observe(this) { result ->
            result?.onSuccess { forecast ->
                fullForecastList = forecast.list
                
                if (fullForecastList.isNotEmpty()) {
                    // Get Today's date from the first item
                    val firstItemDate = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                        java.time.LocalDateTime.parse(fullForecastList.first().dt_txt, formatter).toLocalDate().toString()
                    } else {
                        fullForecastList.first().dt_txt.substring(0, 10)
                    }

                    // 1. Update Daily Forecast (Bottom List)
                    val dailyList = forecast.list
                        .groupBy { it.dt_txt.substring(0, 10) } // Group by YYYY-MM-DD
                        .map { entry ->
                            // For each day, find the item with max temp to represent the day
                            entry.value.maxByOrNull { it.main.temp } ?: entry.value.first()
                        }
                        .filter { 
                            // Exclude Today
                            !it.dt_txt.startsWith(firstItemDate)
                        }
                        .take(5) // Limit to 5 days
                    
                    forecastAdapter.submitList(dailyList)
                    
                    // 2. Update Hourly Forecast (Middle List) - Show only Today's remaining hours
                    showCurrentHourly()
                }
            }
        }
    }

    private fun showCurrentHourly() {
        if (fullForecastList.isEmpty()) return

        // Highlight Main Card, Clear Adapter Selection
        binding.weatherCard.setBackgroundResource(R.drawable.bg_card_selected)
        if (::forecastAdapter.isInitialized) {
            forecastAdapter.clearSelection()
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            // Get the date of the first item (Today)
            val todayDate = java.time.LocalDateTime.parse(fullForecastList.first().dt_txt, formatter).toLocalDate()
            
            // Filter list to show ONLY items for Today
            val todayHourlyList = fullForecastList.filter {
                java.time.LocalDateTime.parse(it.dt_txt, formatter).toLocalDate() == todayDate
            }
            hourlyForecastAdapter.updateData(todayHourlyList)
        } else {
            // Fallback for older SDKs
            val todayDateStr = fullForecastList.first().dt_txt.substring(0, 10)
            val todayHourlyList = fullForecastList.filter { it.dt_txt.startsWith(todayDateStr) }
            hourlyForecastAdapter.updateData(todayHourlyList)
        }
    }

    private fun showDayHourly(dayItem: ForecastItem) {
        // Un-highlight Main Card
        binding.weatherCard.setBackgroundResource(R.drawable.bg_card)

        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                val selectedDateTime = java.time.LocalDateTime.parse(dayItem.dt_txt, formatter)
                val selectedDate = selectedDateTime.toLocalDate()
                
                android.util.Log.d("MainActivity", "Selected Date: $selectedDate")
                
                val dayHourlyList = fullForecastList.filter { 
                    val itemDateTime = java.time.LocalDateTime.parse(it.dt_txt, formatter)
                    itemDateTime.toLocalDate() == selectedDate
                }
                
                android.util.Log.d("MainActivity", "Filtered List Size: ${dayHourlyList.size}")
                
                if (dayHourlyList.isNotEmpty()) {
                    hourlyForecastAdapter.updateData(dayHourlyList)
                } else {
                    showCurrentHourly()
                }
            } else {
                // Fallback for older SDKs
                val selectedDate = dayItem.dt_txt.split(" ")[0]
                val dayHourlyList = fullForecastList.filter { it.dt_txt.startsWith(selectedDate) }
                hourlyForecastAdapter.updateData(dayHourlyList)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showCurrentHourly()
        }
    }

    private fun updateBackground(condition: String?) {
        val gradientRes = when (condition) {
            "Clear" -> R.drawable.bg_gradient // Default blue/purple
            "Clouds" -> R.drawable.bg_gradient_cloudy
            "Rain", "Drizzle", "Thunderstorm" -> R.drawable.bg_gradient_rainy
            "Snow" -> R.drawable.bg_gradient_snow
            else -> R.drawable.bg_gradient
        }
        binding.root.setBackgroundResource(gradientRes)
    }
}