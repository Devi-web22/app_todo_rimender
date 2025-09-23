package com.example.todorimender

import android.database.Cursor
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: databasehelper
    private var userId: Int = -1

    private lateinit var recyclerView: RecyclerView
    private val todoList = mutableListOf<todoAdapter.Todo>()
    private lateinit var adapter: todoAdapter

    private lateinit var btnAddTodo: Button
    private lateinit var edtTitle: EditText
    private lateinit var edtDesc: EditText

    // variabel untuk menandai apakah sedang edit
    private var editingTodoId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = databasehelper(this)
        userId = intent.getIntExtra("USER_ID", -1)

        recyclerView = findViewById(R.id.recyclerViewTodos)
        btnAddTodo = findViewById(R.id.btnAddTodo)
        edtTitle = findViewById(R.id.edtTodoTitle)
        edtDesc = findViewById(R.id.edtTodoDesc)

        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = todoAdapter(
            this,
            todoList,
            onEdit = { todo ->
                // Isi kembali ke EditText untuk update
                edtTitle.setText(todo.title)
                edtDesc.setText(todo.desc)
                editingTodoId = todo.id
                btnAddTodo.text = "Update Todo"
            },
            onDelete = { todo ->
                dbHelper.deleteTodo(todo.id)
                Toast.makeText(this, "Todo dihapus", Toast.LENGTH_SHORT).show()
                loadTodos()
            }
        )

        recyclerView.adapter = adapter

        loadTodos()

        btnAddTodo.setOnClickListener {
            val title = edtTitle.text.toString().trim()
            val desc = edtDesc.text.toString().trim()

            if (title.isEmpty() || desc.isEmpty()) {
                Toast.makeText(this, "Isi judul & deskripsi", Toast.LENGTH_SHORT).show()
            } else {
                if (editingTodoId != null) {
                    // mode update
                    dbHelper.updateTodo(editingTodoId!!, title, desc)
                    Toast.makeText(this, "Todo diperbarui", Toast.LENGTH_SHORT).show()
                    editingTodoId = null
                    btnAddTodo.text = "Tambah Todo"
                } else {
                    // mode tambah
                    dbHelper.addTodo(title, desc, userId)
                    Toast.makeText(this, "Todo ditambahkan", Toast.LENGTH_SHORT).show()
                }

                edtTitle.text.clear()
                edtDesc.text.clear()
                loadTodos()
            }
        }
    }

    private fun loadTodos() {
        todoList.clear()
        val cursor: Cursor = dbHelper.getTodosByUser(userId)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val title = cursor.getString(cursor.getColumnIndexOrThrow("title"))
                val desc = cursor.getString(cursor.getColumnIndexOrThrow("description"))
                todoList.add(todoAdapter.Todo(id, title, desc))
            } while (cursor.moveToNext())
        }
        cursor.close()

        adapter.notifyDataSetChanged()
    }
}
