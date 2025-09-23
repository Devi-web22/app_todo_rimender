package com.example.todorimender

import android.database.Cursor
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
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

    private lateinit var tvStudentName: TextView
    private lateinit var tvClassInfo: TextView

    // untuk mode edit
    private var editingTodoId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // database
        dbHelper = databasehelper(this)
        userId = intent.getIntExtra("USER_ID", -1)

        // view binding manual
        recyclerView = findViewById(R.id.recyclerViewTodos)
        btnAddTodo = findViewById(R.id.btnAddTodo)
        edtTitle = findViewById(R.id.edtTodoTitle)
        edtDesc = findViewById(R.id.edtTodoDesc)
        tvStudentName = findViewById(R.id.tvStudentName)
        tvClassInfo = findViewById(R.id.tvClassInfo)

        // header dummy
        tvStudentName.text = "Halo, Budi!"
        tvClassInfo.text = "Kelas XI IPA 2"

        // RecyclerView setup
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = todoAdapter(
            this,
            todoList,
            onEdit = { todo ->
                edtTitle.setText(todo.title)
                edtDesc.setText(todo.desc)
                editingTodoId = todo.id
                btnAddTodo.text = "Update Tugas"
            },
            onDelete = { todo ->
                dbHelper.deleteTodo(todo.id)
                Toast.makeText(this, "Tugas dihapus", Toast.LENGTH_SHORT).show()
                loadTodos()
            }
        )
        recyclerView.adapter = adapter

        // load awal
        loadTodos()

        // tombol tambah/update
        btnAddTodo.setOnClickListener {
            val title = edtTitle.text.toString().trim()
            val desc = edtDesc.text.toString().trim()

            if (title.isEmpty() || desc.isEmpty()) {
                Toast.makeText(this, "Isi judul & deskripsi", Toast.LENGTH_SHORT).show()
            } else {
                if (editingTodoId != null) {
                    dbHelper.updateTodo(editingTodoId!!, title, desc)
                    Toast.makeText(this, "Tugas diperbarui", Toast.LENGTH_SHORT).show()
                    editingTodoId = null
                    btnAddTodo.text = "Tambah"
                } else {
                    dbHelper.addTodo(title, desc, userId)
                    Toast.makeText(this, "Tugas ditambahkan", Toast.LENGTH_SHORT).show()
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
