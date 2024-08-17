package hotian.dev.radiotool

import android.content.Context
import android.media.MediaPlayer

class AudioReader(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null

    fun playAudio(resourceId: Int) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(context, resourceId)
        mediaPlayer?.start()
    }

    fun stopAudio() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}