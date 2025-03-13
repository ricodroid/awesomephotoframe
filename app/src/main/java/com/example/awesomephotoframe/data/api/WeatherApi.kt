package com.example.awesomephotoframe.data.api

import com.example.awesomephotoframe.data.model.WeatherResponse
import retrofit2.http.GET

interface WeatherApi {
    @GET("v3/e7f7a5b4-8ca9-475d-8e9b-f0bdfb562ea6") // Mocky.io のエンドポイント (発行された URL の末尾)
    suspend fun getWeather(): WeatherResponse
}