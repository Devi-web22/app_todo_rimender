package com.example.todorimender

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

    companion object {
        private const val REQUEST_CODE_DETAIL = 101
        private const val REQUEST_NOTIFICATION_PERMISSION = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
        userId = sharedPref.getInt("USER_ID", -1)
        if (userId == -1) {
            startActivity(Intent(this, login::class.java))
            finish()
            return
        }

        requestNotificationPermissionIfNeeded()

        dbHelper = databasehelper(this)

        recyclerView = findViewById(R.id.recyclerViewTodos)
        btnAddTodo = findViewById(R.id.btnAddTodo)
        edtTitle = findViewById(R.id.edtTodoTitle)
        edtDesc = findViewById(R.id.edtTodoDesc)
        tvStudentName = findViewById(R.id.tvStudentName)
        tvClassInfo = findViewById(R.id.tvClassInfo)
        tvCurrentDate = findViewById(R.id.tvCurrentDate)
        searchView = findViewById(R.id.searchView)
        btnLogout = findViewById(R.id.btnLogout)

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
                val intent = Intent(this, detail::class.java).apply {
                    putExtra("title", todo.title)
                    putExtra("description", todo.desc)
                    putExtra("todoId", todo.id)
                }
                startActivityForResult(intent, REQUEST_CODE_DETAIL)
            }
        )
        recyclerView.adapter = adapter

        loadTodos()

        btnAddTodo.setOnClickListener {
            val title = edtTitle.text.toString().trim()
            val desc = edtDesc.text.toString().trim()
            val deadline = "Tanpa deadline"

            if (title.isEmpty() || desc.isEmpty()) {
                Toast.makeText(this, "Isi judul & deskripsi", Toast.LENGTH_SHORT).show()
            } else {
                if (editingTodoId != null) {
                    dbHelper.updateTodo(editingTodoId!!, title, desc, deadline)
                    Toast.makeText(this, "Tugas diperbarui", Toast.LENGTH_SHORT).show()
                    editingTodoId = null
                    btnAddTodo.text = "Tambah"
                } else {
                    dbHelper.addTodo(title, desc, deadline, userId)
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

        btnLogout.setOnClickListener {
            sharedPref.edit { clear() }
            Toast.makeText(this, "Berhasil logout", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, login::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_DETAIL && resultCode == RESULT_OK) {
            val updatedDeadline = data?.getStringExtra("deadline")
            val updatedTodoId = data?.getIntExtra("todoId", -1) ?: -1

            if (updatedDeadline != null && updatedTodoId != -1) {
                val updatedRows = dbHelper.updateTodoDeadline(updatedTodoId, updatedDeadline)
                Log.d("MainActivity", "Update deadline in DB: $updatedDeadline for todoId: $updatedTodoId, rows affected: $updatedRows")
                loadTodos()
                Toast.makeText(this, "Deadline diperbarui", Toast.LENGTH_SHORT).show()
            }
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
                val deadline = cursor.getString(cursor.getColumnIndexOrThrow("deadline")) ?: "Tanpa deadline"

                Log.d("MainActivity", "Loaded todo: id=$id, title=$title, deadline=$deadline")

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
                    it.deadline.contains(query, ignoreCase = true)
        }
        todoList.clear()
        todoList.addAll(filtered)
        adapter.notifyDataSetChanged()
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_NOTIFICATION_PERMISSION
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(this, "Izin notifikasi diberikan", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Izin notifikasi ditolak", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
