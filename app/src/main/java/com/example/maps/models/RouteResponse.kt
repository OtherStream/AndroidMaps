package com.example.maps.models

data class RouteResponse(
    val routes: List<Route>
)

data class Route(
    val geometry: Geometry,
    val summary: Summary
)

data class Geometry(
    val coordinates: List<List<Double>> // [lon, lat]
)

data class Summary(
    val distance: Double, // metros
    val duration: Double  // segundos
)