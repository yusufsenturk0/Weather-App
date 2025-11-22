package com.example.wetherapp.data.repository

import com.example.wetherapp.data.api.WeatherApi
import com.example.wetherapp.data.model.WeatherResponse
import com.example.wetherapp.data.model.ForecastResponse

class WeatherRepository(private val api: WeatherApi) {
    suspend fun getCurrentWeather(lat: Double, lon: Double, apiKey: String, lang: String = "en"): Result<WeatherResponse> {
        return try {
            val response = api.getCurrentWeather(lat, lon, apiKey, units = "metric", lang = lang)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCurrentWeatherByCity(city: String, apiKey: String, lang: String = "en"): Result<WeatherResponse> {
        return try {
            val response = api.getCurrentWeatherByCity(city, apiKey, units = "metric", lang = lang)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getForecast(city: String, apiKey: String, lang: String = "en"): Result<ForecastResponse> {
        return try {
            val response = api.getForecast(city, apiKey, units = "metric", lang = lang)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCurrentWeatherByLocation(lat: Double, lon: Double, apiKey: String, lang: String = "en"): Result<WeatherResponse> {
        return try {
            val response = api.getCurrentWeatherByLocation(lat, lon, apiKey, units = "metric", lang = lang)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
