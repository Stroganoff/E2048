package com.example.e2048

interface SwipeCallback {
    fun onSwipe(direction: Direction)

    enum class Direction {
        LEFT, RIGHT, UP, DOWN
    }
}