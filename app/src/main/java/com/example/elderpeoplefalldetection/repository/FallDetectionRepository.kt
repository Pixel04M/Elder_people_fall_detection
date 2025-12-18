package com.example.elderpeoplefalldetection.repository

import com.example.elderpeoplefalldetection.data.api.FallDetectionApi
import com.example.elderpeoplefalldetection.data.model.FallRecord
import com.example.elderpeoplefalldetection.data.network.ApiClient

class FallDetectionRepository {
    private val api: FallDetectionApi = ApiClient.fallDetectionApi
    
    suspend fun getAllRecords(studentId: String): Result<List<FallRecord>> {
        return try {
            val response = api.getAllRecords(studentId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.success(emptyList()) // Return empty list instead of failing
                }
            } else {
                Result.failure(Exception("Failed to fetch records: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}", e))
        }
    }
    
    suspend fun deleteAllRecords(studentId: String): Result<Unit> {
        return try {
            val response = api.deleteAllRecords(studentId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete records: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}", e))
        }
    }
}

