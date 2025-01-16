package com.example.keepup

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class WalkingService : Service() {

    private lateinit var locationManager: LocationManager
    private val path = mutableListOf<LatLng>()
    private var totalDistance = 0.0
    private var previousLocation: Location? = null

    override fun onCreate() {
        super.onCreate()

        // Create notification channel
        createNotificationChannel()
        startForeground(1, createNotification("Walking Tracker: Starting..."))

        // Initialize location listener
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                val latLng = LatLng(location.latitude, location.longitude)
                path.add(latLng)

                // Calculate distance
                previousLocation?.let { prev ->
                    totalDistance += prev.distanceTo(location) / 1000.0
                }
                previousLocation = location

                // Update notification
                updateNotification("Distance: ${formatDistance(totalDistance)}")
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: android.os.Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        // Request location updates
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000L, // 1 second
                1f, // 1 meter
                locationListener
            )
        } else {
            stopSelf()
        }

        // Save workout at the end of the day
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                val calendar = Calendar.getInstance()
                if (calendar.get(Calendar.HOUR_OF_DAY) == 23 && calendar.get(Calendar.MINUTE) == 59) {
                    saveWorkoutToFirestore()
                    path.clear()
                    totalDistance = 0.0
                }
                delay(60000L) // Check every minute
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        locationManager.removeUpdates {}
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "walking_service",
            "Walking Service",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun createNotification(content: String): Notification {
        return NotificationCompat.Builder(this, "walking_service")
            .setContentTitle("Walking Tracker")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(content: String) {
        val notification = createNotification(content)
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(1, notification)
    }

    private fun saveWorkoutToFirestore() {
        val firestoreRepository = FirestoreRepository()
        firestoreRepository.saveWorkout(
            type = "Walking",
            elapsedTime = "0", // Set the total time
            distance = totalDistance,
            avgSpeed = 0.0,
            points = 0,
            path = path.map { SimpleLatLng(it.latitude, it.longitude) },
            onSuccess = {
                Log.d("WalkingService", "Workout saved successfully.")
            },
            onFailure = { exception ->
                Log.e("WalkingService", "Failed to save workout: ${exception.message}")
            }
        )
    }

    private fun formatDistance(distance: Double): String {
        return if (distance < 1) {
            "${(distance * 1000).toInt()} m"
        } else {
            "${distance.toInt()} km"
        }
    }
}
