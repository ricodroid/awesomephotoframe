package com.example.awesomephotoframe.data.repository

import com.example.awesomephotoframe.data.api.RetrofitClient
import com.example.awesomephotoframe.data.model.WeatherResponse

class WeatherRepository {
    private val api = RetrofitClient.instance

    suspend fun fetchWeather(): WeatherResponse {
        return api.getWeather()
    }
}