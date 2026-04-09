package com.example.maps.data

import android.content.Context
import org.osmdroid.util.GeoPoint

object PreferencesManager {

    private const val PREFS_NAME = "regreso_prefs"
    private const val KEY_LAT = "home_lat"
    private const val KEY_LON = "home_lon"
    private const val KEY_NAME = "home_name"

    fun saveHome(context: Context, name: String, lat: Double, lon: Double) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString(KEY_NAME, name)
            .putFloat(KEY_LAT, lat.toFloat())
            .putFloat(KEY_LON, lon.toFloat())
            .apply()
    }

    fun getHomeName(context: Context): String =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_NAME, "") ?: ""

    fun getHomeLocation(context: Context): GeoPoint? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lat = prefs.getFloat(KEY_LAT, 0f).toDouble()
        val lon = prefs.getFloat(KEY_LON, 0f).toDouble()
        return if (lat == 0.0 && lon == 0.0) null
        else GeoPoint(lat, lon)
    }
}