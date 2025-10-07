package com.example.todorimender

import android.content.Intent
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

    private lateinit var btnLogout: Button


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
        tvStudentName = findViewById(R.id.tvStudentName)
        tvClassInfo = findViewById(R.id.tvClassInfo)
        tvCurrentDate = findViewById(R.id.tvCurrentDate)
        searchView = findViewById(R.id.searchView)
        btnLogout = findViewById(R.id.btnLogout) // ⬅️ Tombol logout


        val currentDate = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(Date())
        tvCurrentDate.text = currentDate


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
            },
            onClick = { todo ->
                val intent = Intent(this, detail::class.java)
                intent.putExtra("title", todo.title)
                intent.putExtra("description", todo.desc)
                startActivity(intent)
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
                    dbHelper.updateTodo(
                        editingTodoId!!, title, desc,
                        deadline = TODO()
                    )
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


        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                filterTodos(newText.orEmpty())
                return true
            }
        })

        btnAddTodo.setOnClickListener {
            val title = edtTitle.text.toString().trim()
            val desc = edtDesc.text.toString().trim()


            val deadline = intent.getStringExtra("deadline") ?: "Tanpa deadline"

            if (title.isEmpty() || desc.isEmpty()) {
                Toast.makeText(this, "Isi judul & deskripsi", Toast.LENGTH_SHORT).show()
            } else {
                dbHelper.addTodo(title, desc, deadline, userId)
                Toast.makeText(this, "Tugas ditambahkan", Toast.LENGTH_SHORT).show()
                edtTitle.text.clear()
                edtDesc.text.clear()
                loadTodos()
            }
        }


        btnLogout.setOnClickListener {

            Toast.makeText(this, "Berhasil logout", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()

        }
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
                val deadline =
                    cursor.getString(cursor.getColumnIndexOrThrow("deadline")) ?: "Tanpa deadline"

                val todo = todoAdapter.Todo(id, title, desc, deadline)
                todoList.add(todo)
                fullTodoList.add(todo)
            } while (cursor.moveToNext())
        }
        cursor.close()
        adapter.notifyDataSetChanged()
    }

    private fun filterTodos(query: String) {
        val filtered = fullTodoList.filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.desc.contains(query, ignoreCase = true) ||
                    it.deadline.contains(
                        query,
                        ignoreCase = true
                    )
        }

        todoList.clear()
        todoList.addAll(filtered)
        adapter.notifyDataSetChanged()
    }
}


