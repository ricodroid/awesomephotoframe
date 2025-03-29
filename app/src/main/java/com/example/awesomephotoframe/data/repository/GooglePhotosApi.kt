package com.example.awesomephotoframe.data.repository

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface GooglePhotosApi {
    @GET("v1/mediaItems")
    suspend fun listMediaItems(
        @Header("Authorization") authHeader: String,
        @Query("pageToken") pageToken: String? = null
    ): MediaItemsResponse
}

data class MediaItemsResponse(
    @SerializedName("mediaItems")
    val mediaItems: List<MediaItem>?,

    @SerializedName("nextPageToken")
    val nextPageToken: String?
)

data class MediaItem(
    @SerializedName("id") val id: String,
    @SerializedName("baseUrl") val baseUrl: String
)
