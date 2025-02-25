package com.example.skibiditts

import com.yourapp.skibidi.GroqRequest
import com.yourapp.skibidi.GroqResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface GroqApiService {
    @Headers(
        "Authorization: Bearer YOUR_API_KEY_GOES_HERR",  // Replace with your actual API key
        "Content-Type: application/json"
    )
    @POST("chat/completions")  // Correct Groq API endpoint
    fun generateSkibidi(@Body request: GroqRequest): Call<GroqResponse>
}
