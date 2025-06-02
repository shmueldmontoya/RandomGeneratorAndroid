package com.zadel.randomgenerator

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class MemoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MemoryAdapter
    private var memoryList = mutableListOf<MemoryEntry>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memory)

        // Ajustar barra de estado para iconos legibles
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val decorView = window.decorView
            val isDarkTheme = (resources.configuration.uiMode
                    and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
            decorView.systemUiVisibility = if (isDarkTheme) {
                0
            } else {
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }

        recyclerView = findViewById(R.id.recycler_memory)
        recyclerView.layoutManager = LinearLayoutManager(this)

        memoryList = getMemoryList().toMutableList()
        adapter = MemoryAdapter(memoryList,
            onItemClick = { selectedEntry ->
                val resultIntent = Intent()
                resultIntent.putExtra("selected_text", selectedEntry.text)
                setResult(RESULT_OK, resultIntent)
                finish()
            },
            onDeleteClick = { entryToDelete ->
                val pos = memoryList.indexOf(entryToDelete)
                if (pos != -1) {
                    memoryList.removeAt(pos)
                    adapter.notifyItemRemoved(pos)
                    saveMemoryList(memoryList)
                }
            })

        recyclerView.adapter = adapter
    }

    // Inflar menú con solo opción Ajustes
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.memory_menu, menu) // Debe contener solo "action_settings"
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.action_settings -> {
                showSettingsDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showSettingsDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_settings, null)

        // Importante: importa android.widget.Button y android.widget.Switch
        val switchDarkMode = dialogView.findViewById<Switch>(R.id.switch_dark_mode)
        val buttonClearAll = dialogView.findViewById<Button>(R.id.button_clear_all)
        val buttonExport = dialogView.findViewById<Button>(R.id.button_export)
        val buttonImport = dialogView.findViewById<Button>(R.id.button_import)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        buttonClearAll.setOnClickListener {
            dialog.dismiss()
            showClearAllConfirmation()
        }

        buttonExport.setOnClickListener {
            dialog.dismiss()
            exportMemoryToJsonFile()
        }

        buttonImport.setOnClickListener {
            dialog.dismiss()
            val imported = importMemoryFromJsonFile()
            if (imported != null) {
                memoryList.clear()
                memoryList.addAll(imported)
                adapter.notifyDataSetChanged()
                saveMemoryList(memoryList)
                Toast.makeText(this, "Importado correctamente", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No se encontró el archivo", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }


    private fun showClearAllConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Confirmar")
            .setMessage("¿Estás seguro de que quieres borrar todo el historial?")
            .setPositiveButton("Sí") { _, _ ->
                memoryList.clear()
                adapter.notifyDataSetChanged()
                saveMemoryList(memoryList)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun getMemoryList(): List<MemoryEntry> {
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val json = prefs.getString("memory_history", "[]")
        return Gson().fromJson(json, object : TypeToken<List<MemoryEntry>>() {}.type)
    }

    private fun saveMemoryList(list: List<MemoryEntry>) {
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        prefs.edit().putString("memory_history", Gson().toJson(list)).apply()
    }

    private fun exportMemoryToJsonFile() {
        val json = Gson().toJson(memoryList)
        val file = File(getExternalFilesDir(null), "historial.json")
        file.writeText(json)
        Toast.makeText(this, "Exportado a ${file.absolutePath}", Toast.LENGTH_LONG).show()
    }

    private fun importMemoryFromJsonFile(): List<MemoryEntry>? {
        val file = File(getExternalFilesDir(null), "historial.json")
        if (!file.exists()) return null
        val json = file.readText()
        return Gson().fromJson(json, object : TypeToken<List<MemoryEntry>>() {}.type)
    }
}
