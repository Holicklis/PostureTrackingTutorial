package com.example.posturetrackingtutorial

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark

class PoseGraphic(overlay: GraphicOverlay, private val pose: Pose) : GraphicOverlay.Graphic(overlay) {

    private val landmarkPaint = Paint().apply {
        color = Color.GREEN
        strokeWidth = 12f
        style = Paint.Style.FILL
    }

    private val skeletonPaint = Paint().apply {
        color = Color.RED
        strokeWidth = 8f
        style = Paint.Style.STROKE
    }

    override fun draw(canvas: Canvas) {
        if (pose == null) return

        // Draw all landmarks
        pose.allPoseLandmarks.forEach { landmark ->
            if (landmark != null) {
                val x = translateX(landmark.position.x)
                val y = translateY(landmark.position.y)
                canvas.drawCircle(x, y, 12f, landmarkPaint)
            }
        }

        // Draw skeleton connections
        drawSkeleton(canvas, pose)
    }

    private fun drawSkeleton(canvas: Canvas, pose: Pose) {
        val skeletonPairs = arrayOf(
            intArrayOf(PoseLandmark.LEFT_SHOULDER, PoseLandmark.RIGHT_SHOULDER),
            intArrayOf(PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_ELBOW),
            intArrayOf(PoseLandmark.LEFT_ELBOW, PoseLandmark.LEFT_WRIST),
            intArrayOf(PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_ELBOW),
            intArrayOf(PoseLandmark.RIGHT_ELBOW, PoseLandmark.RIGHT_WRIST),
            intArrayOf(PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_HIP),
            intArrayOf(PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_HIP),
            intArrayOf(PoseLandmark.LEFT_HIP, PoseLandmark.RIGHT_HIP),
            intArrayOf(PoseLandmark.LEFT_HIP, PoseLandmark.LEFT_KNEE),
            intArrayOf(PoseLandmark.LEFT_KNEE, PoseLandmark.LEFT_ANKLE),
            intArrayOf(PoseLandmark.RIGHT_HIP, PoseLandmark.RIGHT_KNEE),
            intArrayOf(PoseLandmark.RIGHT_KNEE, PoseLandmark.RIGHT_ANKLE),
            intArrayOf(PoseLandmark.LEFT_SHOULDER, PoseLandmark.RIGHT_HIP),
            intArrayOf(PoseLandmark.RIGHT_SHOULDER, PoseLandmark.LEFT_HIP)
        )

        skeletonPairs.forEach { pair ->
            val first = pose.getPoseLandmark(pair[0])
            val second = pose.getPoseLandmark(pair[1])
            if (first != null && second != null) {
                val startX = translateX(first.position.x)
                val startY = translateY(first.position.y)
                val endX = translateX(second.position.x)
                val endY = translateY(second.position.y)
                canvas.drawLine(startX, startY, endX, endY, skeletonPaint)
            }
        }
    }
}
