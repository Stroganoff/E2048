package com.example.e2048

import android.graphics.Bitmap
import com.example.e2048.sprites.Tile

interface TileManagerCallback {
    fun getBitmap(level: Int): Bitmap
    fun finishedMoving(tile: Tile)
    fun updateScore(newPoints: Int)
    fun reached2048()
}