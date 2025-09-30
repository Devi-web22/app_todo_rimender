package com.example.todorimender

import android.database.Cursor
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.SearchView
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: databasehelper
    private var userId: Int = -1

    private lateinit var recyclerView: RecyclerView
    private val todoList = mutableListOf<todoAdapter.Todo>()
    private val fullTodoList = mutableListOf<todoAdapter.Todo>()
    private lateinit var adapter: todoAdapter

    private lateinit var btnAddTodo: Button
    private lateinit var edtTitle: EditText
    private lateinit var edtDesc: EditText

    private lateinit var tvStudentName: TextView
    private lateinit var tvClassInfo: TextView
    private lateinit var tvCurrentDate: TextView
    private lateinit var searchView: SearchView

    // Untuk mode edit
    private var editingTodoId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inisialisasi DB dan user ID
        dbHelper = databasehelper(this)
        userId = intent.getIntExtra("USER_ID", -1)

        // View Binding
        recyclerView = findViewById(R.id.recyclerViewTodos)
        btnAddTodo = findViewById(R.id.btnAddTodo)
        edtTitle = findViewById(R.id.edtTodoTitle)
        edtDesc = findViewById(R.id.edtTodoDesc)
        tvStudentName = findViewById(R.id.tvStudentName)
        tvClassInfo = findViewById(R.id.tvClassInfo)
        tvCurrentDate = findViewById(R.id.tvCurrentDate)
        searchView = findViewById(R.id.searchView)

        // Tampilkan tanggal otomatis
        val currentDate = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(Date())
        tvCurrentDate.text = currentDate

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

        // Load data awal
        loadTodos()

        // Tambah atau update tugas
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

        // Fitur Pencarian
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterTodos(newText.orEmpty())
                return true
            }
        })
    }

    private fun loadTodos() {
        todoList.clear()
        fullTodoList.clear()

        val cursor: Cursor = dbHelper.getTodosByUser(userId)
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val title = cursor.getString(cursor.getColumnIndexOrThrow("title"))
                val desc = cursor.getString(cursor.getColumnIndexOrThrow("description"))
                val todo = todoAdapter.Todo(id, title, desc)
                todoList.add(todo)
                fullTodoList.add(todo)
            } while (cursor.moveToNext())
        }
        cursor.close()
        adapter.notifyDataSetChanged()
    }

    private fun filterTodos(query: String) {
        val filtered = fullTodoList.filter {
            it.title.contains(query, ignoreCase = true) || it.desc.contains(query, ignoreCase = true)
        }

        todoList.clear()
        todoList.addAll(filtered)
        adapter.notifyDataSetChanged()
    }
}
