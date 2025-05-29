package com.zadel.randomgenerator

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var inputText: EditText
    private lateinit var outputText: EditText
    private lateinit var buttonClear: Button
    private lateinit var buttonCopy: Button
    private lateinit var buttonDownload: Button
    private lateinit var buttonFileInput: Button

    // Mapeo de caracteres para codificación personalizada
    private val charMap = mapOf(
        "a" to "¢", "b" to "£", "c" to "¤", "d" to "¥", "e" to "¦",
        "f" to "§", "g" to "►", "h" to "©", "i" to "ª", "j" to "«",
        "k" to "¬", "l" to "®", "m" to "¯", "n" to "°", "ñ" to "±",
        "o" to "²", "p" to "³", "q" to "µ", "r" to "¶", "s" to "·",
        "t" to "¹", "u" to "º", "v" to "»", "w" to "¼", "x" to "½",
        "y" to "¾", "z" to "ø", "A" to "¢^", "B" to "£^", "C" to "¤^",
        "D" to "¥^", "E" to "¦^", "F" to "§^", "G" to "►^", "H" to "©^",
        "I" to "ª^", "J" to "«^", "K" to "¬^", "L" to "®^", "M" to "¯^",
        "N" to "°^", "Ñ" to "±^", "O" to "²^", "P" to "³^", "Q" to "µ^",
        "R" to "¶^", "S" to "·^", "T" to "¹^", "U" to "º^", "V" to "»^",
        "W" to "¼^", "X" to "½^", "Y" to "¾^", "Z" to "ø^", "Á" to "¢´",
        "É" to "¦´", "Í" to "ª´", "Ó" to "²´", "Ú" to "º´", "á" to "¢´",
        "é" to "¦´", "í" to "ª´", "ó" to "²´", "ú" to "º´", "Ü" to "º¨",
        "ü" to "º¨", " " to "&"
    )

    // Mapa inverso para decodificación
    private val reverseCharMap = charMap.entries.associate { (k, v) -> v to k }

    companion object {
        const val REQUEST_CODE_MEMORY = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        applySavedDarkMode()
        setContentView(R.layout.activity_main)

        setupDrawer()
        setupViews()
        setupInputTransformation()
        setupButtons()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_MEMORY && resultCode == RESULT_OK) {
            val selectedText = data?.getStringExtra("selected_text") ?: return
            val currentText = inputText.text.toString()
            if (currentText.isNotBlank()) {
                AlertDialog.Builder(this)
                    .setMessage("Hay texto en pantalla. ¿Sobrescribirlo?")
                    .setPositiveButton("Sí") { _, _ -> inputText.setText(selectedText) }
                    .setNegativeButton("Cancelar", null)
                    .show()
            } else {
                inputText.setText(selectedText)
            }
        }
    }

    private fun setupDrawer() {
        val drawer = findViewById<LinearLayout>(R.id.end_drawer)
        ViewCompat.setOnApplyWindowInsetsListener(drawer) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val basePadding = (16 * view.resources.displayMetrics.density).toInt()
            view.setPadding(
                basePadding + systemBars.left,
                basePadding + systemBars.top,
                basePadding + systemBars.right,
                basePadding + systemBars.bottom
            )
            insets
        }

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val menuButton = findViewById<ImageButton>(R.id.button_menu)
        menuButton.setOnClickListener { drawerLayout.openDrawer(GravityCompat.END) }
    }

    private fun setupViews() {
        inputText = findViewById(R.id.input_text)
        outputText = findViewById(R.id.output_text)
        buttonClear = findViewById(R.id.button_clear)
        buttonCopy = findViewById(R.id.button_copy)
        buttonDownload = findViewById(R.id.button_download)
        buttonFileInput = findViewById(R.id.button_file_input)
    }

    private fun setupInputTransformation() {
        inputText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { execute() }
        })
    }

    private fun setupButtons() {
        val saveMemoryButton = findViewById<Button>(R.id.button_save_memory)
        val settingsButton = findViewById<Button>(R.id.button_settings)
        val memoryButton = findViewById<Button>(R.id.button_memory)

        saveMemoryButton.setOnClickListener {
            val input = outputText.text.toString()
            if (input.isNotBlank()) {
                saveToMemory(input)
                Toast.makeText(this, "Guardado en memoria", Toast.LENGTH_SHORT).show()
            }
        }

        memoryButton.setOnClickListener {
            val intent = Intent(this, MemoryActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_MEMORY)
        }


        settingsButton.setOnClickListener { showSettingsDialog() }

        buttonClear.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Confirmar limpieza")
                .setMessage("¿Estás seguro que quieres limpiar el texto?")
                .setPositiveButton("Sí") { _, _ -> clear() }
                .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
                .show()
        }

        buttonCopy.setOnClickListener { copy() }

        buttonDownload.setOnClickListener { download() }

        val filePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { openFile(it) }
        }
        buttonFileInput.setOnClickListener { filePicker.launch("text/plain") }
    }

    private fun transformText(text: String, map: Map<String, String>): String {
        val result = StringBuilder()
        var i = 0
        while (i < text.length) {
            val char = text[i].toString()
            val nextChar = if (i + 1 < text.length) text[i + 1].toString() else ""
            val combined = char + nextChar
            when {
                combined in map -> {
                    result.append(map[combined])
                    i += 2
                }
                char in map -> {
                    result.append(map[char])
                    i++
                }
                else -> {
                    result.append(char)
                    i++
                }
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
        val file = File(cacheDir, "output.txt")
        FileOutputStream(file).use { it.write(text.toByteArray()) }
        val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
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

    private fun saveToMemory(newEntryText: String) {
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val gson = Gson()
        val history = getMemoryList().toMutableList()
        val newEntry = MemoryEntry(newEntryText, System.currentTimeMillis())
        history.add(0, newEntry)
        prefs.edit().putString("memory_history", gson.toJson(history)).apply()
    }

    private fun getMemoryList(): List<MemoryEntry> {
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val json = prefs.getString("memory_history", "[]")
        return Gson().fromJson(json, object : TypeToken<List<MemoryEntry>>() {}.type)
    }

    private fun applySavedDarkMode() {
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("dark_mode", false)
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
        window.statusBarColor = ContextCompat.getColor(
            this,
            if (isDarkMode) R.color.status_bar_dark else R.color.status_bar
        )
        val decorView = window.decorView
        val flags = decorView.systemUiVisibility
        decorView.systemUiVisibility = if (isDarkMode) {
            flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        } else {
            flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }

    private fun showSettingsDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_settings, null)
        val switchDarkMode = dialogView.findViewById<Switch>(R.id.switch_dark_mode)
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("dark_mode", false)
        switchDarkMode.isChecked = isDarkMode
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Cerrar", null)
            .create()
        dialog.show()
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("dark_mode", isChecked).apply()
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                AppCompatDelegate.setDefaultNightMode(
                    if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                    else AppCompatDelegate.MODE_NIGHT_NO
                )
                recreate()
            }, 100)
        }
    }
}