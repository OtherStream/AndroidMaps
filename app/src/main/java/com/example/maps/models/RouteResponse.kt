package com.example.regresoacasa.models

data class RouteResponse(
    val routes: List<Route>
)

data class Route(
    val geometry: Geometry,
    val summary: Summary
)

data class Geometry(
    val coordinates: List<List<Double>>
)

data class Summary(
    val distance: Double,
    val duration: Double
)