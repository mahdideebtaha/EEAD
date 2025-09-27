package com.mahdideebtaha.eead.data

object FirebaseService {
    private val db = FirebaseFirestore.getInstance()

    fun uploadBenchmark(result: BenchmarkResult, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("benchmarks")
            .add(result)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }
}