package com.example.todorimender

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.app.DatePickerDialog
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent


class detail : AppCompatActivity() {

    private lateinit var tvTitle: TextView
    private lateinit var tvDesc: TextView
    private lateinit var tvDeadline: TextView
    private lateinit var btnPickDate: Button
    private lateinit var btnSetReminder: Button

    private var deadlineCalendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        tvTitle = findViewById(R.id.tvDetailTitle)
        tvDesc = findViewById(R.id.tvDetailDesc)
        tvDeadline = findViewById(R.id.tvDeadline)
        btnPickDate = findViewById(R.id.btnPickDate)
        btnSetReminder = findViewById(R.id.btnSetReminder)

        val title = intent.getStringExtra("title")
        val desc = intent.getStringExtra("description")

        tvTitle.text = title
        tvDesc.text = desc

        // Pilih tanggal deadline
        btnPickDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(this, { _, y, m, d ->
                deadlineCalendar.set(y, m, d)
                val dateStr = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
                    .format(deadlineCalendar.time)
                tvDeadline.text = dateStr
            }, year, month, day)

            datePicker.show()
        }

        btnSetReminder.setOnClickListener {
            val context = this
            val title = tvTitle.text.toString()
            val desc = tvDesc.text.toString()

            val intent = Intent(context, ReminderReceiver::class.java).apply {
                putExtra("title", title)
                putExtra("description", desc)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                deadlineCalendar.timeInMillis,
                pendingIntent
            )

            Toast.makeText(context, "Pengingat berhasil diatur!", Toast.LENGTH_LONG).show()
        }
    }
}