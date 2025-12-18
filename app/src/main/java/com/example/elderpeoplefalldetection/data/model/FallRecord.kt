package com.example.elderpeoplefalldetection.data.model

import com.google.gson.annotations.SerializedName

data class FallRecord(
    @SerializedName("id")
    val id: Int? = null,
    
    @SerializedName("student_id")
    val studentId: String,
    
    @SerializedName("title")
    val title: String? = null,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("phone")
    val phone: String? = null,
    
    @SerializedName("url")
    val url: String? = null,
    
    @SerializedName("age")
    val age: Int? = null,
    
    @SerializedName("is_it_true")
    val isItTrue: Boolean? = null,
    
    @SerializedName("is_it_really_true")
    val isItReallyTrue: Boolean? = null,
    
    @SerializedName("color")
    val color: String? = null,
    
    @SerializedName("size")
    val size: String? = null,
    
    @SerializedName("price")
    val price: Double? = null,
    
    @SerializedName("type")
    val type: String? = null,
    
    @SerializedName("date")
    val date: String? = null,
    
    @SerializedName("another_date")
    val anotherDate: String? = null,
    
    @SerializedName("integer_one")
    val integerOne: Int? = null,
    
    @SerializedName("integer_two")
    val integerTwo: Int? = null,
    
    @SerializedName("integer_three")
    val integerThree: Int? = null,
    
    @SerializedName("integer_four")
    val integerFour: Int? = null,
    
    @SerializedName("integer_five")
    val integerFive: Int? = null,
    
    @SerializedName("integer_six")
    val integerSix: Int? = null,
    
    @SerializedName("integer_seven")
    val integerSeven: Int? = null,
    
    @SerializedName("double_one")
    val doubleOne: Double? = null,
    
    @SerializedName("double_two")
    val doubleTwo: Double? = null,
    
    @SerializedName("double_three")
    val doubleThree: Double? = null,
    
    @SerializedName("double_four")
    val doubleFour: Double? = null,
    
    @SerializedName("double_five")
    val doubleFive: Double? = null,
    
    @SerializedName("double_six")
    val doubleSix: Double? = null,
    
    @SerializedName("double_seven")
    val doubleSeven: Double? = null,
    
    @SerializedName("created_at")
    val createdAt: String? = null,
    
    @SerializedName("updated_at")
    val updatedAt: String? = null,
    
    @SerializedName("int_list")
    val intList: List<Int>? = null,
    
    @SerializedName("text_list")
    val textList: List<String>? = null,
    
    @SerializedName("double_list")
    val doubleList: List<Double>? = null
) {
    // Helper properties to extract fall detection data
    val fallDetected: Boolean
        get() = title?.contains("Fall", ignoreCase = true) == true || 
                description?.contains("Fall", ignoreCase = true) == true ||
                isItTrue == true
    
    val timestamp: String
        get() = createdAt ?: updatedAt ?: ""
    
    // Extract heartbeat from integer fields (using integer_one)
    val heartbeat: Int?
        get() = integerOne
    
    // Extract location from double fields (using double_one for lat, double_two for lon)
    val location: Location?
        get() = if (doubleOne != null && doubleTwo != null) {
            Location(doubleOne, doubleTwo)
        } else null
    
    // Extract acceleration from double fields
    val acceleration: Acceleration?
        get() = if (doubleThree != null && doubleFour != null && doubleFive != null) {
            Acceleration(doubleThree.toFloat(), doubleFour.toFloat(), doubleFive.toFloat())
        } else null
    
    // Extract gyroscope from double fields
    val gyroscope: Gyroscope?
        get() = if (doubleSix != null && doubleSeven != null && integerTwo != null) {
            // Using doubleSix, doubleSeven, and integerTwo for gyro (or adjust as needed)
            Gyroscope(doubleSix.toFloat(), doubleSeven.toFloat(), integerTwo.toFloat())
        } else null
}

data class Acceleration(
    val x: Float,
    val y: Float,
    val z: Float
)

data class Gyroscope(
    val x: Float,
    val y: Float,
    val z: Float
)

data class Location(
    val latitude: Double,
    val longitude: Double
)

data class ApiResponse<T>(
    @SerializedName("code")
    val code: Int,
    
    @SerializedName("status")
    val status: String,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: T
)
