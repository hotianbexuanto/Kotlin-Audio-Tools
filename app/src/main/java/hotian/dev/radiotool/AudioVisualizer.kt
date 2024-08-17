package hotian.dev.radiotool

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View

class AudioVisualizer(context: Context) : View(context) {

    private val paint: Paint = Paint().apply {
        color = android.graphics.Color.RED
        strokeWidth = 2f
    }

    private var audioData: ByteArray? = null

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        audioData?.let {
            drawWaveform(canvas, it)
        }
    }

    fun updateVisualizer(audioData: ByteArray) {
        this.audioData = audioData
        invalidate() // 刷新视图
    }

    private fun drawWaveform(canvas: Canvas, audioData: ByteArray) {
        val centerY = height / 2f
        val widthPerSample = width.toFloat() / audioData.size
        var x = 0f

        for (i in audioData.indices) {
            val sample = audioData[i].toInt()
            val scaledHeight = (sample / 128f) * centerY
            canvas.drawLine(x, centerY - scaledHeight, x, centerY + scaledHeight, paint)
            x += widthPerSample
        }
    }
}