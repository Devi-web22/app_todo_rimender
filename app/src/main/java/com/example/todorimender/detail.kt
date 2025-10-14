package com.example.todorimender

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class detail : AppCompatActivity() {

    private lateinit var tvTitle: TextView
    private lateinit var tvDesc: TextView
    private lateinit var tvDeadline: TextView
    private lateinit var btnPickDateTime: Button
    private lateinit var btnSetReminder: Button

    private var deadlineCalendar = Calendar.getInstance()
    private var isDeadlineSet = false
    private var selectedDeadline: String? = null

    private var todoId: Int = -1

    private lateinit var dbHelper: databasehelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        tvTitle = findViewById(R.id.tvDetailTitle)
        tvDesc = findViewById(R.id.tvDetailDesc)
        tvDeadline = findViewById(R.id.tvDeadline)
        btnPickDateTime = findViewById(R.id.btnPickDateTime)
        btnSetReminder = findViewById(R.id.btnSetReminder)

        val title = intent.getStringExtra("title")
        val desc = intent.getStringExtra("description")
        todoId = intent.getIntExtra("todoId", -1)

        tvTitle.text = title
        tvDesc.text = desc

        dbHelper = databasehelper(this)


        if (todoId != -1) {
            val cursor = dbHelper.getTodoById(todoId)
            if (cursor.moveToFirst()) {
                val deadline = cursor.getString(cursor.getColumnIndexOrThrow("deadline"))
                tvDeadline.text = deadline ?: "Tanpa deadline"
            }
            cursor.close()
        }

        btnPickDateTime.setOnClickListener {
            showDatePicker()
        }

        btnSetReminder.setOnClickListener {
            if (!isDeadlineSet) {
                Toast.makeText(this, "Silakan pilih tanggal dan waktu deadline terlebih dahulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val now = Calendar.getInstance()
            if (deadlineCalendar.timeInMillis <= now.timeInMillis) {
                Toast.makeText(this, "Deadline harus di masa depan", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            setAlarm()


            if (todoId != -1 && selectedDeadline != null) {
                dbHelper.updateTodoDeadline(todoId, selectedDeadline!!)
            }


            val intent = Intent()
            intent.putExtra("deadline", selectedDeadline)
            intent.putExtra("todoId", todoId)
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    private fun showDatePicker() {
        val now = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                deadlineCalendar.set(Calendar.YEAR, year)
                deadlineCalendar.set(Calendar.MONTH, month)
                deadlineCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                showTimePicker()
            },
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH),
            now.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.datePicker.minDate = now.timeInMillis
        datePicker.show()
    }

    private fun showTimePicker() {
        val now = Calendar.getInstance()
        val timePicker = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                deadlineCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                deadlineCalendar.set(Calendar.MINUTE, minute)
                deadlineCalendar.set(Calendar.SECOND, 0)
                deadlineCalendar.set(Calendar.MILLISECOND, 0)

                updateDeadlineText()
                isDeadlineSet = true
            },
            now.get(Calendar.HOUR_OF_DAY),
            now.get(Calendar.MINUTE),
            true
        )
        timePicker.show()
    }

    private fun updateDeadlineText() {
        val format = SimpleDateFormat("dd MMMM yyyy HH:mm", Locale("id", "ID"))
        selectedDeadline = format.format(deadlineCalendar.time)
        tvDeadline.text = selectedDeadline
    }

    private fun setAlarm() {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

        val intent = Intent(this, ReminderReceiver::class.java).apply {
            putExtra("title", tvTitle.text.toString())
            putExtra("description", tvDesc.text.toString())
            putExtra("todoId", todoId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            todoId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            deadlineCalendar.timeInMillis,
            pendingIntent
        )

        Toast.makeText(this, "Pengingat berhasil diatur!", Toast.LENGTH_LONG).show()
    }
}
