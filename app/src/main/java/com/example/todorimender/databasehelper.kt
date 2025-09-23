package com.example.todorimender

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class databasehelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "todo_app.db"
        private const val DATABASE_VERSION = 1

        // USER TABLE
        private const val TABLE_USERS = "users"
        private const val COLUMN_USER_ID = "id"
        private const val COLUMN_USERNAME = "username"
        private const val COLUMN_PASSWORD = "password"

        private const val TABLE_TODOS = "todos"
        private const val COLUMN_TODO_ID = "id"
        private const val COLUMN_TODO_TITLE = "title"
        private const val COLUMN_TODO_DESC = "description"
        private const val COLUMN_TODO_USER_ID = "user_id"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createUsersTable = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USERNAME TEXT UNIQUE,
                $COLUMN_PASSWORD TEXT
            )
        """.trimIndent()

        val createTodosTable = """
            CREATE TABLE $TABLE_TODOS (
                $COLUMN_TODO_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TODO_TITLE TEXT,
                $COLUMN_TODO_DESC TEXT,
                $COLUMN_TODO_USER_ID INTEGER,
                FOREIGN KEY($COLUMN_TODO_USER_ID) REFERENCES $TABLE_USERS($COLUMN_USER_ID)
            )
        """.trimIndent()

        db.execSQL(createUsersTable)
        db.execSQL(createTodosTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TODOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }



    fun registerUser(username: String, password: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USERNAME, username)
            put(COLUMN_PASSWORD, password)
        }

        val result = db.insert(TABLE_USERS, null, values)
        return result != -1L
    }

    fun loginUser(username: String, password: String): Int? {
        val db = readableDatabase
        val query = "SELECT $COLUMN_USER_ID FROM $TABLE_USERS WHERE $COLUMN_USERNAME = ? AND $COLUMN_PASSWORD = ?"
        val cursor = db.rawQuery(query, arrayOf(username, password))

        var userId: Int? = null
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID))
        }
        cursor.close()
        return userId
    }



    fun addTodo(title: String, description: String, userId: Int): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TODO_TITLE, title)
            put(COLUMN_TODO_DESC, description)
            put(COLUMN_TODO_USER_ID, userId)
        }
        return db.insert(TABLE_TODOS, null, values)
    }

    fun getTodosByUser(userId: Int): Cursor {
        val db = readableDatabase
        return db.query(
            TABLE_TODOS,
            null,
            "$COLUMN_TODO_USER_ID = ?",
            arrayOf(userId.toString()),
            null,
            null,
            "$COLUMN_TODO_ID DESC"
        )
    }

    fun updateTodo(todoId: Int, title: String, description: String): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TODO_TITLE, title)
            put(COLUMN_TODO_DESC, description)
        }
        return db.update(TABLE_TODOS, values, "$COLUMN_TODO_ID = ?", arrayOf(todoId.toString()))
    }

    fun deleteTodo(todoId: Int): Int {
        val db = writableDatabase
        return db.delete(TABLE_TODOS, "$COLUMN_TODO_ID = ?", arrayOf(todoId.toString()))
    }


}
