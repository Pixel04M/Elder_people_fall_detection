package com.example.elderpeoplefalldetection.data.api

import com.example.elderpeoplefalldetection.data.model.ApiResponse
import com.example.elderpeoplefalldetection.data.model.FallRecord
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Query

interface FallDetectionApi {
    @GET("records/all")
    suspend fun getAllRecords(
        @Query("student_id") studentId: String
    ): Response<ApiResponse<List<FallRecord>>>
    
    @DELETE("records/all")
    suspend fun deleteAllRecords(
        @Query("student_id") studentId: String
    ): Response<ApiResponse<Any>>
}

