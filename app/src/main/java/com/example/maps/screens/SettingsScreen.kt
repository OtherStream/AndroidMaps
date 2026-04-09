package com.example.maps.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.maps.data.PreferencesManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    // Cargar valores guardados
    var homeName by remember {
        mutableStateOf(PreferencesManager.getHomeName(context))
    }
    var homeLat by remember {
        mutableStateOf(
            PreferencesManager.getHomeLocation(context)?.latitude?.toString() ?: ""
        )
    }
    var homeLon by remember {
        mutableStateOf(
            PreferencesManager.getHomeLocation(context)?.longitude?.toString() ?: ""
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurar mi casa") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            OutlinedTextField(
                value = homeName,
                onValueChange = { homeName = it },
                label = { Text("Nombre (ej. Mi casa)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = homeLat,
                onValueChange = { homeLat = it },
                label = { Text("Latitud (ej. 20.1234)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )

            OutlinedTextField(
                value = homeLon,
                onValueChange = { homeLon = it },
                label = { Text("Longitud (ej. -101.5678)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Tip: abre Google Maps, mantén presionado tu casa y copia los números que aparecen abajo.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Button(
                onClick = {
                    val lat = homeLat.toDoubleOrNull()
                    val lon = homeLon.toDoubleOrNull()

                    when {
                        lat == null || lon == null ->
                            Toast.makeText(context, "Ingresa coordenadas válidas", Toast.LENGTH_SHORT).show()
                        lat !in -90.0..90.0 || lon !in -180.0..180.0 ->
                            Toast.makeText(context, "Coordenadas fuera de rango", Toast.LENGTH_SHORT).show()
                        else -> {
                            PreferencesManager.saveHome(
                                context,
                                homeName.ifEmpty { "Mi casa" },
                                lat,
                                lon
                            )
                            Toast.makeText(context, "Casa guardada ✓", Toast.LENGTH_SHORT).show()
                            onBack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar dirección")
            }
        }
    }
}