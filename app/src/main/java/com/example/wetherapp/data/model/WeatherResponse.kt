package com.example.wetherapp.data.model

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    val main: Main,
    val weather: List<Weather>,
    val name: String,
    val wind: Wind
)

data class Main(
    val temp: Double,
    val humidity: Int,
    @SerializedName("feels_like")
    val feelsLike: Double
)

data class Weather(
    val main: String,
    val description: String,
    val icon: String
)

data class Wind(
    val speed: Double
)
