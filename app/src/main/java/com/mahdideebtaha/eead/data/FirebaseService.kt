package com.mahdideebtaha.eead.data

import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.mahdideebtaha.eead.model.BenchmarkResult

object FirebaseService {

    private val db: FirebaseFirestore
        get() = Firebase.firestore

    fun uploadBenchmark(result: BenchmarkResult, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("benchmarks")
            .add(result)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }
}