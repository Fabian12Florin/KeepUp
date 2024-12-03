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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun RunningDetailsScreen() {
    val context = LocalContext.current
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val coroutineScope = rememberCoroutineScope()

    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    val cameraPositionState = rememberCameraPositionState()

    var isRunning by remember { mutableStateOf(false) }
    var elapsedTime by remember { mutableStateOf(0L) } // Stochează timpul trecut într-o variabilă

    var totalDistance by remember { mutableStateOf(0.0) } // Stochează distanța parcursă
    var previousLocation by remember { mutableStateOf<Location?>(null) }

    var compassRotation by remember { mutableStateOf(0f) }

    // Launcher pentru cererea permisiunii de locație
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (!isGranted) {
                Toast.makeText(context, "Location permission is needed for this feature", Toast.LENGTH_LONG).show()
            }
        }
    )

    // Listener pentru senzorul de orientare
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

    LaunchedEffect(isRunning) {
        if (isRunning) {
            while (isRunning) {
                delay(1000L) // Incrementare la fiecare secundă
                elapsedTime += 1
            }
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getLastKnownLocation(locationManager) { location ->
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    userLocation = latLng
                    coroutineScope.launch {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(latLng, 15f)
                        )
                    }
                }
            }
            startLocationUpdates(locationManager) { location ->
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    userLocation = latLng

                    // Calcularea distanței parcursă
                    previousLocation?.let { prevLocation ->
                        totalDistance += prevLocation.distanceTo(location) / 1000.0 // Conversie la km
                    }
                    previousLocation = location

                    coroutineScope.launch {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(latLng, 15f)
                        )
                    }
                }
            }
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        // Înregistrează listener-ul pentru senzorul de orientare
        sensorManager.registerListener(
            sensorEventListener,
            sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
            SensorManager.SENSOR_DELAY_UI
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            // Dezactivează listener-ul pentru senzorul de orientare
            sensorManager.unregisterListener(sensorEventListener)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Harta într-un card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState
                ) {
                    userLocation?.let { location ->
                        val markerState = rememberMarkerState(position = location)
                        Marker(
                            state = markerState,
                            title = "Current Location"
                        )
                    }
                }
            }

            // Compasul peste hartă, în colțul din dreapta sus
            Icon(
                imageVector = Icons.Default.Explore,
                contentDescription = "Compass",
                tint = Color.Black,
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .rotate(-compassRotation) // Rotirea iconiței în funcție de orientare
            )
        }

        Spacer(modifier = Modifier.height(8.dp)) // Mai puțin spațiu între harta și cardul de detalii

        // Card pentru informațiile despre activitate
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
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
                    fontSize = 18.sp,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = formatElapsedTime(elapsedTime),
                    fontSize = 32.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Distance", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "${totalDistance.roundToInt()} km", fontSize = 18.sp, color = Color.Black)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Calories", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "382 kcal", fontSize = 18.sp, color = Color.Black)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Avg. Speed", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "12.3 km/h", fontSize = 18.sp, color = Color.Black)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp)) // Mai puțin spațiu între cardul de detalii și buton

        // Buton de Start / Stop
        Button(
            onClick = {
                isRunning = !isRunning
                if (!isRunning) {
                    previousLocation = null // Resetarea locației anterioare la oprire
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp), // Ajustează spațiul din partea de sus
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isRunning) Color(0xFFFF5252) else Color(0xFF00C853)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = if (isRunning) "Stop" else "Start",
                fontSize = 20.sp,
                color = Color.White
            )
        }
    }

}

// Funcție pentru a formata timpul trecut
private fun formatElapsedTime(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return String.format("%02d:%02d:%02d", hours, minutes, secs)
}

// Funcție pentru a obține ultima locație cunoscută
private fun getLastKnownLocation(
    locationManager: LocationManager,
    onLocationReceived: (Location?) -> Unit
) {
    val provider = LocationManager.GPS_PROVIDER
    if (locationManager.isProviderEnabled(provider)) {
        val location = locationManager.getLastKnownLocation(provider)
        onLocationReceived(location)
    } else {
        onLocationReceived(null)
    }
}

// Funcție pentru a începe actualizările locației
private fun startLocationUpdates(
    locationManager: LocationManager,
    onLocationReceived: (Location?) -> Unit
) {
    val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            onLocationReceived(location)
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    locationManager.requestLocationUpdates(
        LocationManager.GPS_PROVIDER,
        1000L, // Request updates every 1 second
        5f,    // Minimum distance of 5 meters to get updates
        locationListener
    )
}
