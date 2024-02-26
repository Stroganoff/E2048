package com.example.e2048.sprites

import android.content.res.Resources
import android.graphics.Bitmap.createScaledBitmap
import android.graphics.BitmapFactory.decodeResource
import android.graphics.Canvas
import com.example.e2048.R

class Grid(
    resources: Resources,
    private val screenWidth: Int,
    private val screenHeight: Int,
    private val standardSize: Int
) : Sprite {

    private val grid = decodeResource(
        resources,
        R.drawable.grid
    ).let {
        createScaledBitmap(
            it,
            standardSize * 4,
            standardSize * 4,
            false
        )
    }

    override fun draw(canvas: Canvas) {
        canvas.drawBitmap(
            grid,
            (screenWidth - grid.width) / 2f,
            (screenHeight - grid.height) / 2f,
            null
        )
    }

    override fun update() {}
}