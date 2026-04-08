package com.example.maps

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.maps.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    companion object {
        const val PREFS_NAME = "regreso_prefs"
        const val KEY_HOME_LAT = "home_lat"
        const val KEY_HOME_LON = "home_lon"
        const val KEY_HOME_NAME = "home_name"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Cargar valores guardados
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        binding.etHomeName.setText(prefs.getString(KEY_HOME_NAME, ""))
        binding.etHomeLat.setText(prefs.getFloat(KEY_HOME_LAT, 0f).takeIf { it != 0f }?.toString() ?: "")
        binding.etHomeLon.setText(prefs.getFloat(KEY_HOME_LON, 0f).takeIf { it != 0f }?.toString() ?: "")

        binding.btnSave.setOnClickListener {
            val name = binding.etHomeName.text.toString().trim()
            val latStr = binding.etHomeLat.text.toString().trim()
            val lonStr = binding.etHomeLon.text.toString().trim()

            if (latStr.isEmpty() || lonStr.isEmpty()) {
                Toast.makeText(requireContext(), "Ingresa latitud y longitud", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val lat = latStr.toDoubleOrNull()
            val lon = lonStr.toDoubleOrNull()

            if (lat == null || lon == null ||
                lat !in -90.0..90.0 || lon !in -180.0..180.0) {
                Toast.makeText(requireContext(), "Coordenadas inválidas", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            prefs.edit()
                .putString(KEY_HOME_NAME, name.ifEmpty { "Mi casa" })
                .putFloat(KEY_HOME_LAT, lat.toFloat())
                .putFloat(KEY_HOME_LON, lon.toFloat())
                .apply()

            Toast.makeText(requireContext(), "Casa guardada ✓", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}