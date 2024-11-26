package com.example.posturetrackingtutorial

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import androidx.camera.core.CameraSelector

class GraphicOverlay(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private val lock = Object()
    private var previewWidth = 0
    private var previewHeight = 0
    private var widthScaleFactor = 1.0f
    private var heightScaleFactor = 1.0f
    private var facing = CameraSelector.LENS_FACING_BACK

    private val graphics = mutableSetOf<Graphic>()

    // Base class for custom graphics
    abstract class Graphic(private val overlay: GraphicOverlay) {
        abstract fun draw(canvas: Canvas)

        fun scaleX(horizontal: Float) = horizontal * overlay.widthScaleFactor
        fun scaleY(vertical: Float) = vertical * overlay.heightScaleFactor

        fun translateX(x: Float): Float {
            return if (overlay.facing == CameraSelector.LENS_FACING_FRONT) {
                overlay.width - scaleX(x) // Mirror effect for front camera
            } else {
                scaleX(x)
            }
        }

        fun translateY(y: Float) = scaleY(y)

        fun getOverlay() = overlay
    }

    // Clears all graphics
    fun clear() {
        synchronized(lock) {
            graphics.clear()
        }
        postInvalidate()
    }

    // Adds a graphic to the overlay
    fun add(graphic: Graphic) {
        synchronized(lock) {
            graphics.add(graphic)
        }
        postInvalidate()
    }

    // Removes a graphic from the overlay
    fun remove(graphic: Graphic) {
        synchronized(lock) {
            graphics.remove(graphic)
        }
        postInvalidate()
    }

    // Sets the camera info (preview size and facing direction)
    fun setCameraInfo(previewWidth: Int, previewHeight: Int, facing: Int) {
        synchronized(lock) {
            this.previewWidth = previewWidth
            this.previewHeight = previewHeight
            this.facing = facing
            // Update scale factors based on the actual view size
            if (width != 0 && height != 0) {z
                widthScaleFactor = width.toFloat() / previewWidth
                heightScaleFactor = height.toFloat() / previewHeight
                // Debug logs
                println("GraphicOverlay - Width Scale Factor: $widthScaleFactor, Height Scale Factor: $heightScaleFactor")
            }
        }
        postInvalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        synchronized(lock) {
            if (previewWidth != 0 && previewHeight != 0) {
                widthScaleFactor = w.toFloat() / previewWidth
                heightScaleFactor = h.toFloat() / previewHeight
                // Debug logs
                println("GraphicOverlay onSizeChanged - Width: $w, Height: $h")
                println("GraphicOverlay onSizeChanged - Updated Width Scale Factor: $widthScaleFactor, Height Scale Factor: $heightScaleFactor")
            }
        }
        postInvalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        synchronized(lock) {
            graphics.forEach { graphic -> graphic.draw(canvas) }
        }
    }
}
