package hotian.dev.radiotool

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import java.nio.ByteBuffer

class FileAudioReader(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private var audioRecord: AudioRecord? = null
    private var audioDataCallback: AudioDataCallback? = null

    // 使用 URI 播放音频文件
    fun playAudioFromFile(fileUri: Uri) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(context, fileUri)
            prepare()
            start()
        }
    }

    fun stopAudio() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null

        // 释放 AudioRecord 资源
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
    }

    // 设置音频数据回调
    fun setAudioDataCallback(callback: AudioDataCallback) {
        this.audioDataCallback = callback
        startAudioRecording()
    }

    // 启动音频录制以获取数据
    private fun startAudioRecording() {
        val sampleRate = 44100 // 音频采样率
        val bufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        ).apply {
            startRecording()
            // 在另一个线程中读取音频数据
            Thread {
                val buffer = ByteArray(bufferSize)
                while (true) {
                    val read = read(buffer, 0, buffer.size)
                    if (read > 0) {
                        val byteBuffer = ByteBuffer.wrap(buffer.copyOf(read))
                        audioDataCallback?.onAudioDataAvailable(byteBuffer)
                    }
                }
            }.start()
        }
    }
}