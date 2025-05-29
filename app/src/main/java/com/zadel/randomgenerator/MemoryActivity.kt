package com.zadel.randomgenerator

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson

class MemoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MemoryAdapter
    private var memoryList = mutableListOf<MemoryEntry>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memory)

        // Ajustar el color de la barra de estado para iconos legibles
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val decorView = window.decorView
            val isDarkTheme = (resources.configuration.uiMode
                    and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
            decorView.systemUiVisibility = if (isDarkTheme) {
                0 // iconos claros para tema oscuro
            } else {
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR // iconos oscuros para tema claro
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

    private fun getMemoryList(): List<MemoryEntry> {
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val json = prefs.getString("memory_history", "[]")
        return Gson().fromJson(json, object : com.google.gson.reflect.TypeToken<List<MemoryEntry>>() {}.type)
    }

    private fun saveMemoryList(list: List<MemoryEntry>) {
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        prefs.edit().putString("memory_history", Gson().toJson(list)).apply()
    }
}
