package com.example.wetherapp.util

import com.example.wetherapp.R

object WeatherIconMapper {
    fun getIconResource(iconCode: String?): Int {
        return when (iconCode) {
            "01d" -> R.drawable.ic_weather_01d
            "01n" -> R.drawable.ic_weather_01n
            "02d" -> R.drawable.ic_weather_02d
            "02n" -> R.drawable.ic_weather_02n
            "03d" -> R.drawable.ic_weather_03d
            "03n" -> R.drawable.ic_weather_03n
            "04d" -> R.drawable.ic_weather_04d
            "04n" -> R.drawable.ic_weather_04n
            "09d" -> R.drawable.ic_weather_09d
            "09n" -> R.drawable.ic_weather_09n
            "10d" -> R.drawable.ic_weather_10d
            "10n" -> R.drawable.ic_weather_10n
            "11d" -> R.drawable.ic_weather_11d
            "11n" -> R.drawable.ic_weather_11n
            "13d" -> R.drawable.ic_weather_13d
            "13n" -> R.drawable.ic_weather_13n
            "50d" -> R.drawable.ic_weather_50d
            "50n" -> R.drawable.ic_weather_50n
            else -> R.drawable.ic_weather_01d // Default to clear sky if unknown
        }
    }
}
