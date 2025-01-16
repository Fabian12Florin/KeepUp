package com.example.keepup

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RunningDetailsScreen(navController: NavController) {
    val context = LocalContext.current
    val locationManager = remember {
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }
    val sensorManager = remember {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }
    val coroutineScope = rememberCoroutineScope()

    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(userLocation ?: LatLng(45.0, 25.0), 15f) // Setează un zoom de 15f
    }
    val path = remember { mutableStateListOf<LatLng>() }

    var isRunning by remember { mutableStateOf(false) }
    var elapsedTime by remember { mutableStateOf(0L) }
    var totalDistance by remember { mutableStateOf(0.0) }
    var previousLocation by remember { mutableStateOf<Location?>(null) }

    var realTimeSpeed by remember { mutableStateOf(0.0f) }  // speed in m/s
    var averageSpeed by remember { mutableStateOf(0.0) }    // speed in km/h
    var compassRotation by remember { mutableStateOf(0f) }

    // Points placeholder
    val currentPoints by remember { mutableStateOf(50) }

    // Instantiate FirestoreRepository (no Room)
    val firestoreRepository = FirestoreRepository()

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

    // Handle End Workout Button
    val onEndWorkout: () -> Unit = {
        isRunning = false

        val workoutType = "Running"
        val formattedTime = formatElapsedTime(elapsedTime)
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val roundedDistance = String.format("%.2f", totalDistance).toDouble()
        val roundedAvgSpeed = averageSpeed.roundToInt()

        firestoreRepository.saveWorkout(
            type = workoutType,
            elapsedTime = formattedTime,
            distance = roundedDistance.toDouble(),
            avgSpeed = roundedAvgSpeed.toDouble(),
            points = currentPoints,
            path = path.map { SimpleLatLng(it.latitude, it.longitude) }, // Conversie către SimpleLatLng
            onSuccess = { workoutId ->
                Log.d("Firestore", "Workout saved successfully with ID: $workoutId")
                navController.navigate("workoutDetails/$workoutId")
            }
        ) { exception ->
            Log.e("Firestore", "Failed to save workout: ${exception.message}")
        }
    }

    // Compass sensor listener
    val sensorEventListener = remember {
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event?.sensor?.type == Sensor.TYPE_ORIENTATION) {
                    compassRotation = event.values[0]
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
    }

    // Register compass listener
    LaunchedEffect(Unit) {
        sensorManager.registerListener(
            sensorEventListener,
            sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
            SensorManager.SENSOR_DELAY_UI
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            sensorManager.unregisterListener(sensorEventListener)
        }
    }

    // Update camera when user location changes
    LaunchedEffect(userLocation) {
        userLocation?.let { location ->
            coroutineScope.launch {
                cameraPositionState.animate(CameraUpdateFactory.newLatLng(location))
            }
        }
    }

    // Increment time and compute avg speed when running
    LaunchedEffect(isRunning) {
        if (isRunning) {
            while (isRunning) {
                delay(1000L)
                elapsedTime += 1
                if (totalDistance > 0 && elapsedTime > 0) {
                    averageSpeed = totalDistance / (elapsedTime / 3600.0) // km/h
                }
            }
        }
    }

    // Request location updates if permission granted
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationUpdates(context, locationManager, cameraPositionState, coroutineScope) { location ->
                userLocation = location
            }
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Listen for location updates
    DisposableEffect(Unit) {
        val locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                val latLng = LatLng(location.latitude, location.longitude)
                userLocation = latLng
                if (isRunning) path.add(latLng)

                // Calculate distance
                previousLocation?.let { prev ->
                    totalDistance += prev.distanceTo(location) / 1000.0
                }
                previousLocation = location

                // Speed in m/s
                realTimeSpeed = location.speed
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
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

    // Styling
    val backgroundColor = Color(0xFFF2F2F2)
    val primaryColor = Color(0xFF6200EE)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp) // Outer padding for entire screen
        ) {
            // Map Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.45f)
                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Box {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState
                    ) {
                        val markerState = remember { MarkerState(position = userLocation ?: LatLng(45.0, 25.0)) }

                        userLocation?.let { location ->
                            markerState.position = location // Actualizează poziția markerului
                            Marker(
                                state = markerState,
                                title = "Current Location"
                            )
                        }

                        if (path.isNotEmpty()) {
                            Polyline(
                                points = path.toList(),
                                color = Color(0xFF29B6F6),
                                width = 5f
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.Explore,
                        contentDescription = "Compass",
                        tint = Color.Black,
                        modifier = Modifier
                            .size(60.dp)
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .rotate(-compassRotation)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Points Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(android.R.drawable.ic_menu_info_details),
                        contentDescription = "Points",
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Points: $currentPoints",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Stats Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.25f)
                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Time Elapsed",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = primaryColor
                    )
                    Text(
                        text = formatElapsedTime(elapsedTime),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        StatItem(
                            label = "Dist.",
                            value = formatDistance(totalDistance),
                            color = Color(0xFF66BB6A)
                        )

                        StatItem(
                            label = "Speed",
                            value = "${(realTimeSpeed * 3.6).roundToInt()} km/h",
                            color = Color(0xFFFF7043)
                        )

                        StatItem(
                            label = "Avg.",
                            value = "${averageSpeed.roundToInt()} km/h",
                            color = Color(0xFF29B6F6)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.2f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = { isRunning = !isRunning },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRunning) Color(0xFFFF5252) else Color(0xFF00C853)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isRunning) "Stop Running" else "Start Running",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        onEndWorkout()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9800)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "End Workout",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}

private fun formatDistance(distance: Double): String {
    return if (distance < 1) {
        "${(distance * 1000).roundToInt()} m" // Convertim kilometri în metri
    } else {
        String.format("%.2f km", distance) // Afișăm kilometri cu 2 zecimale
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
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
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
