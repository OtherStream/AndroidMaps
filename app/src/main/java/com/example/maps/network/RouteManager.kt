package com.example.regresoacasa.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.osmdroid.util.GeoPoint
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RouteManager {

    // ← Tu API key gratis de openrouteservice.org
    private const val API_KEY = "TU_API_KEY_AQUI"

    private val api: RouteApiService by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl("https://api.openrouteservice.org/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RouteApiService::class.java)
    }

    suspend fun getRoute(
        originLat: Double, originLon: Double,
        destLat: Double, destLon: Double
    ): List<GeoPoint>? {
        return try {
            val body = mapOf(
                "coordinates" to listOf(
                    listOf(originLon, originLat),
                    listOf(destLon, destLat)
                )
            )
            val response = api.getRoute(API_KEY, body)
            response.routes.firstOrNull()?.geometry?.coordinates?.map { coord ->
                GeoPoint(coord[1], coord[0]) // ORS devuelve [lon, lat]
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}