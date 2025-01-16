package com.example.keepup

import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun CyclingDetailsScreen(navController: NavController) {
    val context = LocalContext.current
    val locationManager = remember {
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }
    val coroutineScope = rememberCoroutineScope()

    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    val cameraPositionState = rememberCameraPositionState()
    val path = remember { mutableStateListOf<LatLng>() }

    var isCycling by remember { mutableStateOf(false) }
    var elapsedTime by remember { mutableStateOf(0L) }
    var totalDistance by remember { mutableStateOf(0.0) }
    var maxSpeed by remember { mutableStateOf(0.0f) }
    var currentSpeed by remember { mutableStateOf(0.0f) }
    var altitudeChange by remember { mutableStateOf(0.0) }
    var previousLocation by remember { mutableStateOf<Location?>(null) }

    // Permission launcher for GPS
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (!isGranted) {
                Toast.makeText(context, "Location permission is required.", Toast.LENGTH_LONG).show()
            } else {
                requestLocationUpdates(context, locationManager, cameraPositionState, coroutineScope) { location ->
                    userLocation = location
                }
            }
        }
    )

    // Track elapsed time and compute max speed when cycling
    LaunchedEffect(isCycling) {
        if (isCycling) {
            while (isCycling) {
                delay(1000L)
                elapsedTime += 1
                if (currentSpeed > maxSpeed) maxSpeed = currentSpeed
            }
        }
    }

    // Request location updates if permission granted
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)
            == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationUpdates(context, locationManager, cameraPositionState, coroutineScope) { location ->
                userLocation = location
            }
        } else {
            locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Listen for location updates
    DisposableEffect(Unit) {
        val locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                val latLng = LatLng(location.latitude, location.longitude)
                userLocation = latLng
                if (isCycling) path.add(latLng)

                // Calculate distance
                previousLocation?.let { prev ->
                    val distance = prev.distanceTo(location) / 1000.0
                    totalDistance += distance

                    // Calculate altitude change
                    altitudeChange += (location.altitude - prev.altitude)
                }
                previousLocation = location

                // Update current speed
                currentSpeed = location.speed
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)
            == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000L,
                0f,
                locationListener
            )
        }

        onDispose {
            locationManager.removeUpdates(locationListener)
        }
    }

    // UI Content
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // Map Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.45f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState
                ) {
                    userLocation?.let { location ->
                        Marker(
                            state = rememberMarkerState(position = location),
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE) // Marker albastru
                        )
                    }
                    if (path.isNotEmpty()) {
                        Polyline(
                            points = path.toList(),
                            color = Color.Blue,
                            width = 5f
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Cycling Stats
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Cycling Stats", style = MaterialTheme.typography.titleLarge)

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        StatItem(label = "Time", value = formatElapsedTime(elapsedTime))
                        StatItem(label = "Distance", value = "${totalDistance.roundToInt()} km")
                        StatItem(label = "Max Speed", value = "${(maxSpeed * 3.6).roundToInt()} km/h")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        StatItem(label = "Altitude Gain", value = "${altitudeChange.roundToInt()} m")
                        StatItem(label = "Speed", value = "${(currentSpeed * 3.6).roundToInt()} km/h")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { isCycling = !isCycling },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCycling) Color.Red else Color.Green
                    )
                ) {
                    Text(text = if (isCycling) "Stop Cycling" else "Start Cycling")
                }

                Button(
                    onClick = {
                        isCycling = false
                        navController.navigate("home")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
                ) {
                    Text(text = "Save & Exit")
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.bodySmall)
        Text(text = value, style = MaterialTheme.typography.titleMedium)
    }
}

private fun requestLocationUpdates(
    context: Context,
    locationManager: LocationManager,
    cameraPositionState: CameraPositionState,
    coroutineScope: CoroutineScope,
    onLocationReceived: (LatLng?) -> Unit
) {
    val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)

    var locationFound = false

    for (provider in providers) {
        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            val lastKnownLocation = locationManager.getLastKnownLocation(provider)
            if (lastKnownLocation != null) {
                val latLng = LatLng(lastKnownLocation.latitude, lastKnownLocation.longitude)
                onLocationReceived(latLng)
                coroutineScope.launch {
                    cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                }
                locationFound = true
                break
            }
        }
    }

    if (!locationFound) {
        onLocationReceived(null)
    }
}
