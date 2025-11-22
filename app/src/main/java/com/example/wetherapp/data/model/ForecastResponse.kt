package com.example.wetherapp.data.model

data class ForecastResponse(
    val list: List<ForecastItem>,
    val city: City
)
// Force update

data class ForecastItem(
    val dt: Long,
    val main: Main,
    val weather: List<Weather>,
    val dt_txt: String
)

data class City(
    val name: String,
    val country: String
)
