package hotian.dev.radiotool

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import hotian.dev.radiotool.databinding.ActivityMainBinding
import android.view.View
import java.nio.ByteBuffer
import android.content.Context

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding: ActivityMainBinding
        get() = checkNotNull(_binding) { "Activity has been destroyed" }

    private lateinit var fileAudioReader: FileAudioReader
    private lateinit var morseCodeConverter: MorseCodeConverter
    private lateinit var audioVisualizer: AudioVisualizer

    private val REQUEST_PERMISSIONS = 101
    private val REQUEST_CODE_AUDIO_FILE = 102

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 初始化工具类实例
        fileAudioReader = FileAudioReader(this)
        morseCodeConverter = MorseCodeConverter()

        // 绑定 AudioVisualizer
        audioVisualizer = AudioVisualizer(this)
        binding.audioVisualizerContainer.addView(audioVisualizer)

        // 使用 ViewBinding 获取 UI 组件
        val btnPlayFileAudio: Button = binding.btnPlayFileAudio
        val btnStopAudio: Button = binding.btnStopAudio
        val edtMorseInput: EditText = binding.edtMorseInput
        val btnConvertToMorse: Button = binding.btnConvertToMorse
        val txtMorseOutput: TextView = binding.txtMorseOutput

        // 请求权限
        requestPermissions()

        // 按钮点击事件处理
        btnPlayFileAudio.setOnClickListener {
            openAudioFilePicker()  // 调用文件选择器
        }

        btnStopAudio.setOnClickListener {
            fileAudioReader.stopAudio()  // 使用 FileAudioReader 停止音频
        }

        btnConvertToMorse.setOnClickListener {
            val text = edtMorseInput.text.toString()
            val morseCode = morseCodeConverter.toMorseCode(text)
            txtMorseOutput.text = morseCode
        }

        // 设置音频数据回调
        fileAudioReader.setAudioDataCallback(object : AudioDataCallback {
            override fun onAudioDataAvailable(data: ByteBuffer) {
                // Convert ByteBuffer to ByteArray
                val byteArray = ByteArray(data.remaining())
                data.get(byteArray)

                // 更新可视化器
                audioVisualizer.updateVisualizer(byteArray)
            }
        })
    }

    // 打开音频文件选择器
    private fun openAudioFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "audio/*"  // 只显示音频文件
        }
        startActivityForResult(intent, REQUEST_CODE_AUDIO_FILE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_AUDIO_FILE && resultCode == Activity.RESULT_OK) {
            data?.data?.also { uri ->
                try {
                    fileAudioReader.playAudioFromFile(uri)  // 使用 FileAudioReader 播放音频
                } catch (e: Exception) {
                    Toast.makeText(this, "Error playing audio file", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
            } ?: run {
                Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun requestPermissions() {
        // 请求必要的权限
        val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
        )
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                this,
                permissions,
                REQUEST_PERMISSIONS
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Toast.makeText(this, "All permissions granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permissions required for full functionality", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        // 确保在Activity销毁时释放资源
        fileAudioReader.stopAudio()
    }
}

// 回调接口定义
interface AudioDataCallback {
    fun onAudioDataAvailable(data: ByteBuffer)
}