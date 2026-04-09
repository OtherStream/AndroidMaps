package com.example.maps.screens

import android.Manifest
import android.content.Context
import android.graphics.Color
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.maps.data.PreferencesManager
import com.example.maps.location.LocationHelper
import com.example.maps.network.RouteManager
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(onNavigateToSettings: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(false) }
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var myLocationOverlay by remember { mutableStateOf<MyLocationNewOverlay?>(null) }

    // Permisos de ubicación
    val locationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // Inicializar osmdroid una sola vez
    LaunchedEffect(Unit) {
        Configuration.getInstance().apply {
            userAgentValue = context.packageName
            load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        }
        if (!locationPermissions.allPermissionsGranted) {
            locationPermissions.launchMultiplePermissionRequest()
        }
    }

    // Activar ubicación cuando se concedan permisos
    LaunchedEffect(locationPermissions.allPermissionsGranted) {
        if (locationPermissions.allPermissionsGranted) {
            myLocationOverlay?.enableMyLocation()
            myLocationOverlay?.enableFollowLocation()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Regreso a Casa") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Configurar casa"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    val homeLocation = PreferencesManager.getHomeLocation(context)
                    val homeName = PreferencesManager.getHomeName(context)

                    if (homeLocation == null) {
                        Toast.makeText(
                            context,
                            "Primero configura tu casa en ajustes ⚙️",
                            Toast.LENGTH_LONG
                        ).show()
                        return@ExtendedFloatingActionButton
                    }

                    if (!locationPermissions.allPermissionsGranted) {
                        locationPermissions.launchMultiplePermissionRequest()
                        return@ExtendedFloatingActionButton
                    }

                    scope.launch {
                        isLoading = true
                        val myLocation = LocationHelper.getCurrentLocation(context)

                        if (myLocation == null) {
                            Toast.makeText(context, "No se pudo obtener tu ubicación", Toast.LENGTH_SHORT).show()
                            isLoading = false
                            return@launch
                        }

                        val route = RouteManager.getRoute(
                            myLocation.latitude, myLocation.longitude,
                            homeLocation.latitude, homeLocation.longitude
                        )

                        isLoading = false

                        if (route == null || route.isEmpty()) {
                            Toast.makeText(context, "No se pudo calcular la ruta", Toast.LENGTH_SHORT).show()
                            return@launch
                        }

                        // Dibujar ruta en el mapa
                        mapView?.let { map ->
                            map.overlays.removeAll { it is Polyline || it is Marker }

                            val polyline = Polyline().apply {
                                setPoints(route)
                                outlinePaint.color = Color.parseColor("#1976D2")
                                outlinePaint.strokeWidth = 10f
                            }
                            map.overlays.add(polyline)

                            val marker = Marker(map).apply {
                                position = homeLocation
                                title = homeName.ifEmpty { "Mi casa" }
                                snippet = "Tu destino"
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            }
                            map.overlays.add(marker)

                            myLocationOverlay?.let { map.overlays.add(it) }

                            val boundingBox = BoundingBox.fromGeoPoints(route)
                            map.zoomToBoundingBox(boundingBox, true, 100)
                            map.invalidate()
                        }
                    }
                },
                icon = { Icon(Icons.Default.Home, contentDescription = null) },
                text = { Text("Llevarme a casa") }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {

            // Mapa osmdroid dentro de Compose usando AndroidView
            AndroidView(
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(15.0)

                        val overlay = MyLocationNewOverlay(
                            GpsMyLocationProvider(ctx), this
                        ).also {
                            myLocationOverlay = it
                            if (locationPermissions.allPermissionsGranted) {
                                it.enableMyLocation()
                                it.enableFollowLocation()
                                it.runOnFirstFix {
                                    post {
                                        controller.setCenter(it.myLocation)
                                        controller.setZoom(16.0)
                                    }
                                }
                            }
                        }
                        overlays.add(overlay)
                        mapView = this
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Indicador de carga
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}