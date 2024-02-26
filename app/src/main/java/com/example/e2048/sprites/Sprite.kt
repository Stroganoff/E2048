package com.example.e2048.sprites

import android.graphics.Canvas

interface Sprite {
    fun draw(canvas: Canvas)
    fun update()
}