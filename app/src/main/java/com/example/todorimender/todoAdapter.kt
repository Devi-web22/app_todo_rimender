package com.example.todorimender

import android.content.Context
import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class todoAdapter(
    private val context: Context,
    private val todos: MutableList<Todo>,
    private val onEdit: (Todo) -> Unit,
    private val onDelete: (Todo) -> Unit,
    private val onClick: (Todo) -> Unit
) : RecyclerView.Adapter<todoAdapter.TodoViewHolder>() {

    data class Todo(val id: Int, val title: String, val desc: String)

    inner class TodoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTodoTitle)
        val tvDesc: TextView = itemView.findViewById(R.id.tvTodoDesc)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btnEditTodo)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDeleteTodo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_todo, parent, false)
        return TodoViewHolder(view)
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        val todo = todos[position]
        holder.tvTitle.text = todo.title
        holder.tvDesc.text = todo.desc

        holder.btnEdit.setOnClickListener {
            playSound()
            onEdit(todo)
        }

        holder.btnDelete.setOnClickListener {
            playSound()
            onDelete(todo)
        }

        // Klik item untuk buka detail
        holder.itemView.setOnClickListener {
            playSound()
            onClick(todo)
        }
    }

    override fun getItemCount(): Int = todos.size

    private fun playSound() {
        val mp = MediaPlayer.create(context, android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
        mp.setOnCompletionListener { player -> player.release() }
        mp.start()
    }
}
