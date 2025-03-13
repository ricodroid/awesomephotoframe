package com.example.awesomephotoframe.data.repository

import com.example.awesomephotoframe.data.model.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    @GET("v3/e7f7a5b4-8ca9-475d-8e9b-f0bdfb562ea6")
    suspend fun getWeather(): WeatherResponse
}