package com.example.keepup

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

class FirestoreRepository {

    fun saveWorkout(
        type: String,
        elapsedTime: String,
        distance: Double,
        avgSpeed: Double,
        points: Int,
        path: List<SimpleLatLng>,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val firestore = FirebaseFirestore.getInstance()
        val workoutsCollection = firestore.collection("workouts")

        val workoutData = hashMapOf(
            "type" to type,
            "date" to java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()),
            "elapsedTime" to elapsedTime,
            "distance" to distance,
            "avgSpeed" to avgSpeed,
            "points" to points,
            "path" to path
        )

        workoutsCollection.add(workoutData)
            .addOnSuccessListener { documentReference ->
                onSuccess(documentReference.id)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun fetchWorkoutDetails(
        workoutId: String,
        onSuccess: (Workout) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("workouts").document(workoutId).get()
            .addOnSuccessListener { documentSnapshot ->
                val workout = documentSnapshot.toObject<Workout>()
                if (workout != null) {
                    onSuccess(workout)
                } else {
                    onFailure(Exception("Workout not found"))
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun fetchWorkouts(
        onResult: (List<Workout>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("workouts")
            .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val workouts = querySnapshot.documents.mapNotNull { document ->
                    val workout = document.toObject(Workout::class.java)
                    workout?.copy(id = document.id)
                }
                onResult(workouts)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
}
