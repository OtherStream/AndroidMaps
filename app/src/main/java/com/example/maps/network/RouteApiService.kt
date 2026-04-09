package com.example.maps.network

import com.example.maps.models.RouteResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface RouteApiService {
    @POST("v2/directions/driving-car/geojson")
    suspend fun getRoute(
        @Header("Authorization") apiKey: String,
        @Body body: Map<String, Any>
    ): RouteResponse
}