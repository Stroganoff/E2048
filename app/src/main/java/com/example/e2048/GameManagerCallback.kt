package com.example.e2048

interface GameManagerCallback {
    fun gameOver()
    fun updateScore(newPoints: Int)
    fun reached2048()
}
