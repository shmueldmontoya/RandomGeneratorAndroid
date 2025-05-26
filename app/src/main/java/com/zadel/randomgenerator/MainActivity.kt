package com.zadel.randomgenerator

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.WindowCompat
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import androidx.appcompat.app.AlertDialog

class MainActivity : AppCompatActivity() {

    private lateinit var inputText: EditText
    private lateinit var outputText: EditText
    private lateinit var buttonClear: Button
    private lateinit var buttonCopy: Button
    private lateinit var buttonDownload: Button
    private lateinit var buttonFileInput: Button

    private val charMap = mapOf(
        "a" to "¢", "b" to "£", "c" to "¤", "d" to "¥", "e" to "¦", "f" to "§", "g" to "►", "h" to "©", "i" to "ª",
        "j" to "«", "k" to "¬", "l" to "®", "m" to "¯", "n" to "°", "ñ" to "±", "o" to "²", "p" to "³", "q" to "µ",
        "r" to "¶", "s" to "·", "t" to "¹", "u" to "º", "v" to "»", "w" to "¼", "x" to "½", "y" to "¾", "z" to "ø",
        "A" to "¢^", "B" to "£^", "C" to "¤^", "D" to "¥^", "E" to "¦^", "F" to "§^", "G" to "►^", "H" to "©^", "I" to "ª^",
        "J" to "«^", "K" to "¬^", "L" to "®^", "M" to "¯^", "N" to "°^", "Ñ" to "±^", "O" to "²^", "P" to "³^", "Q" to "µ^",
        "R" to "¶^", "S" to "·^", "T" to "¹^", "U" to "º^", "V" to "»^", "W" to "¼^", "X" to "½^", "Y" to "¾^", "Z" to "ø^",
        "Á" to "¢´", "É" to "¦´", "Í" to "ª´", "Ó" to "²´", "Ú" to "º´", "á" to "¢´", "é" to "¦´", "í" to "ª´", "ó" to "²´",
        "ú" to "º´", "Ü" to "º¨", "ü" to "º¨", " " to "&"
    )

    private val reverseCharMap = charMap.entries.associate { (k, v) -> v to k }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        setContentView(R.layout.activity_main)

        // Inicializar vistas
        inputText = findViewById(R.id.input_text)
        outputText = findViewById(R.id.output_text)
        buttonClear = findViewById(R.id.button_clear)
        buttonCopy = findViewById(R.id.button_copy)
        buttonDownload = findViewById(R.id.button_download)
        buttonFileInput = findViewById(R.id.button_file_input)

        // Configurar responsividad para pantallas pequeñas
        val headerTitle = findViewById<TextView>(R.id.header_title)
        val headerSubtitle = findViewById<TextView>(R.id.header_subtitle)
        val headerSubtitle2 = findViewById<TextView>(R.id.header_subtitle2)
        if (resources.displayMetrics.widthPixels < 415 * resources.displayMetrics.density) {
            headerTitle.visibility = TextView.GONE
            headerSubtitle.visibility = TextView.GONE
            headerSubtitle2.visibility = TextView.VISIBLE
        }

        // Transformar texto en tiempo real
        inputText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                execute()
            }
        })

        // Botón Limpiar
        buttonClear.setOnClickListener {
            buttonClear.setOnClickListener {
                AlertDialog.Builder(this)
                    .setTitle("Confirmar limpieza")
                    .setMessage("¿Estás seguro que quieres limpiar el texto?")
                    .setPositiveButton("Sí") { dialog, which ->
                        // Si el usuario confirma, limpia los campos
                        clear()
                    }
                    .setNegativeButton("No") { dialog, which ->
                        // Si el usuario cancela, simplemente cierra el diálogo
                        dialog.dismiss()
                    }
                    .show()
            }

        }

        // Botón Copiar
        buttonCopy.setOnClickListener {
            copy()
        }

        // Botón Descargar
        buttonDownload.setOnClickListener {
            download()
        }

        // Botón Seleccionar Archivo
        val filePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { openFile(it) }
        }
        buttonFileInput.setOnClickListener {
            filePicker.launch("text/plain")
        }
    }

    private fun transformText(text: String, map: Map<String, String>): String {
        val result = StringBuilder()
        var i = 0
        while (i < text.length) {
            val char = text[i].toString()
            val nextChar = if (i + 1 < text.length) text[i + 1].toString() else ""
            val combined = char + nextChar
            if (combined in map) {
                result.append(map[combined])
                i += 2
            } else if (char in map) {
                result.append(map[char])
                i++
            } else {
                result.append(char)
                i++
            }
        }
        return result.toString()
    }

    private fun execute() {
        val text = inputText.text.toString()
        val isEncoded = reverseCharMap.keys.any { text.contains(it) }
        outputText.setText(transformText(text, if (isEncoded) reverseCharMap else charMap))
    }

    private fun clear() {
        inputText.text.clear()
        outputText.text.clear()
    }

    private fun copy() {
        val text = outputText.text.toString()
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("Random Generator Output", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Texto copiado", Toast.LENGTH_SHORT).show()
    }

    private fun download() {
        val text = outputText.text.toString()
        if (text.isEmpty()) {
            Toast.makeText(this, "No hay texto para descargar", Toast.LENGTH_SHORT).show()
            return
        }

        // Crear archivo temporal
        val fileName = "output.txt" // En una versión futura, usaré un diálogo para personalizar el nombre
        val file = File(cacheDir, fileName)
        FileOutputStream(file).use { it.write(text.toByteArray()) }

        // Compartir archivo usando FileProvider
        val uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, "Descargar como .txt"))
    }

    private fun openFile(uri: android.net.Uri) {
        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val text = inputStream.bufferedReader().readText()
                inputText.setText(text)
                execute()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al leer el archivo", Toast.LENGTH_SHORT).show()
        }
    }
}