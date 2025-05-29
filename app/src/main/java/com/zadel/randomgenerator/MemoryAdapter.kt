package com.zadel.randomgenerator

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class MemoryAdapter(
    private val items: MutableList<MemoryEntry>,
    private val onItemClick: (MemoryEntry) -> Unit,
    private val onDeleteClick: (MemoryEntry) -> Unit
) : RecyclerView.Adapter<MemoryAdapter.MemoryViewHolder>() {

    inner class MemoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textPreview: TextView = itemView.findViewById(R.id.text_preview)
        val timestamp: TextView = itemView.findViewById(R.id.text_timestamp)
        val deleteButton: ImageButton = itemView.findViewById(R.id.button_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_memory, parent, false)
        return MemoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemoryViewHolder, position: Int) {
        val entry = items[position]
        holder.textPreview.text = entry.text.take(100)
        holder.timestamp.text = formatTimestamp(entry.timestamp)

        holder.itemView.setOnClickListener {
            onItemClick(entry)
        }

        holder.deleteButton.setOnClickListener {
            val context = holder.itemView.context
            android.app.AlertDialog.Builder(context)
                .setTitle("¿Eliminar entrada?")
                .setMessage("¿Estás seguro de que quieres eliminar esta entrada del historial?")
                .setPositiveButton("Sí") { _, _ ->
                    onDeleteClick(entry)
                }
                .setNegativeButton("No", null)
                .show()
        }
    }

    override fun getItemCount() = items.size

    private fun formatTimestamp(timeMillis: Long): String {
        val sdf = SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date(timeMillis))
    }

    fun removeAt(position: Int) {
        items.removeAt(position)
        notifyItemRemoved(position)
    }
}
