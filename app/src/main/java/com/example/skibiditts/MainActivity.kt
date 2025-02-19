package com.example.skibiditts


import android.graphics.Color
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.yourapp.skibidi.GroqRequest
import com.yourapp.skibidi.GroqResponse
import com.yourapp.skibidi.Message
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

class MainActivity : AppCompatActivity(), OnInitListener {

    private lateinit var tts: TextToSpeech
    private lateinit var inputText: EditText
    private lateinit var generateButton: Button
    private lateinit var mildButton: Button
    private lateinit var fullButton: Button
    private lateinit var outputText: TextView
    private var skibidiIntensity = "mild"
    private lateinit var progressBar: ProgressBar
    private lateinit var ttsButton : Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        inputText = findViewById(R.id.inputText)
        generateButton = findViewById(R.id.generateButton)
        mildButton = findViewById(R.id.mildButton)
        fullButton = findViewById(R.id.fullButton)
        outputText = findViewById(R.id.outputText)
        progressBar = findViewById(R.id.progressBar)
        ttsButton = findViewById(R.id.ttsButton)

        // Initialize TTS engine
        tts = TextToSpeech(this, this)
        ttsButton.setOnClickListener {
            speakText()
        }

        mildButton.setOnClickListener {
            skibidiIntensity = "mild"
            Toast.makeText(this, "Mild mode selected", Toast.LENGTH_SHORT).show()
            println("Mild button clicked")
            updateButtonUI()
        }

        fullButton.setOnClickListener {
            skibidiIntensity = "full"
            Toast.makeText(this, "Full mode selected", Toast.LENGTH_SHORT).show()
            println("Full button clicked")
            updateButtonUI()
        }

        generateButton.setOnClickListener {
            val userInput = inputText.text.toString()
            if (userInput.isNotEmpty()) {
                generateSkibidiText(userInput)
                Toast.makeText(this, "Generating Skibidi text...", Toast.LENGTH_SHORT).show()
            }
            else Toast.makeText(this, "Please enter text", Toast.LENGTH_SHORT).show()
        }
        updateButtonUI()
    }
    private fun updateButtonUI() {
        if (skibidiIntensity == "mild") {
            mildButton.setBackgroundColor(Color.GREEN)
            fullButton.setBackgroundColor(Color.LTGRAY)
        } else {
            fullButton.setBackgroundColor(Color.RED)
            mildButton.setBackgroundColor(Color.LTGRAY)
        }
    }
    // TTS initialization callback
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val langResult = tts.setLanguage(Locale.US) // Set language to US English
            if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                outputText.text = "TTS language is not supported."
            }
        } else {
            outputText.text = "TTS initialization failed!"
        }
    }

    // Function to generate Skibidi text
    private fun generateSkibidiText(input: String) {
        if (tts.isSpeaking) {
            tts.stop()
        }
        progressBar.visibility = View.VISIBLE
        outputText.text = ""
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.groq.com/openai/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(GroqApiService::class.java)

        // Change the prompt based on the selected intensity

        val prompt = if (skibidiIntensity == "full") {
            "Translate/explain this to/in full brainrot slang: $input"
        } else {
            "Translate/explain this to/in mild brainrot slang: $input"
        }

        val request = GroqRequest(model = "llama3-8b-8192",messages = listOf(Message("user", prompt)))

        apiService.generateSkibidi(request).enqueue(object : Callback<GroqResponse> {
            override fun onResponse(call: Call<GroqResponse>, response: Response<GroqResponse>) {
                progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    val skibidiText = response.body()?.choices?.firstOrNull()?.message?.content
                    outputText.text = skibidiText ?: "No response received!"

                } else {
                    outputText.text = "Error: ${response.code()}"
                }
            }

            override fun onFailure(call: Call<GroqResponse>, t: Throwable) {
                Log.e("API_ERROR", "Failed to connect: ${t.message}")
                outputText.text = "Failed to connect: ${t.message}"
                progressBar.visibility = View.GONE
                outputText.text = "Failed to connect!"
            }
        })
    }

    // Function to speak the generated Skibidi text
    private fun speakText() {
        val text = outputText.text.toString()
        if (text.isNotEmpty()) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        } else {
            Toast.makeText(this, "No text to speak!", Toast.LENGTH_SHORT).show()
        }
    }


    // Cleanup TTS resources when the activity is destroyed
    override fun onDestroy() {
        if (tts != null) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}
