package com.example.maps.network

import com.example.maps.models.RouteResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

// Interfaz Retrofit para OpenRouteService
interface OrsApiService {
    @POST("v2/directions/driving-car/geojson")
    suspend fun getRoute(
        @Header("Authorization") apiKey: String,
        @Body body: Map<String, Any>
    ): RouteResponse
}

object RouteManager {

    // ← Obtén tu API KEY GRATIS en https://openrouteservice.org/
    private const val ORS_API_KEY = "TU_API_KEY_AQUI"
    private const val BASE_URL = "https://api.openrouteservice.org/"

    private val api: OrsApiService by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OrsApiService::class.java)
    }

    /**
     * Obtiene la ruta entre dos puntos.
     * @return lista de pares (lat, lon) o null si falla
     */
    suspend fun getRoute(
        originLat: Double, originLon: Double,
        destLat: Double, destLon: Double
    ): List<Pair<Double, Double>>? {
        return try {
            val body = mapOf(
                "coordinates" to listOf(
                    listOf(originLon, originLat),   // ORS usa [lon, lat]
                    listOf(destLon, destLat)
                )
            )
            val response = api.getRoute(ORS_API_KEY, body)
            // Convertir [lon, lat] → (lat, lon) para osmdroid
            response.routes.firstOrNull()?.geometry?.coordinates?.map {
                Pair(it[1], it[0])
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}