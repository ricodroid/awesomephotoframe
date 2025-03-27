package com.example.awesomephotoframe.data.repository

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Header

interface GooglePhotosApi {
    @GET("v1/mediaItems")
    suspend fun listMediaItems(
        @Header("Authorization") authHeader: String
    ): MediaItemsResponse
}

data class MediaItemsResponse(
    @SerializedName("mediaItems") val mediaItems: List<MediaItem>?
)

data class MediaItem(
    @SerializedName("id") val id: String,
    @SerializedName("baseUrl") val baseUrl: String
)
