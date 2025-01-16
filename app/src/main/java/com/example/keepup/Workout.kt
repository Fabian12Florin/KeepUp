package com.example.keepup

import com.google.android.gms.maps.model.LatLng

data class Workout(
    val id: String = "",         // ID-ul documentului
    val type: String = "",       // Activity type (e.g., Running, Cycling)
    val date: String = "",       // Activity date
    val elapsedTime: String = "", // Total time (formatted as hh:mm:ss)
    val distance: Double = 0.0,   // Distance covered in km
    val avgSpeed: Double = 0.0,   // Average speed in km/h
    val steps: Int = 0,
    val points: Int = 0,          // Points earned
    val path: List<SimpleLatLng> = emptyList() // Lista de coordonate
) {
    // Constructor fără argumente pentru Firebase
    constructor() : this("", "", "", "", 0.0, 0.0, 0, 0, emptyList())
}
