package com.yourapp.skibidi

data class GroqResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: Message
)
