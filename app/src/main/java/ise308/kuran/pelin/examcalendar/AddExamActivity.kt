package ise308.kuran.pelin.examcalendar

import android.content.ContentValues
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.CalendarView
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import java.lang.Exception

class AddExamActivity : AppCompatActivity() {
    val dbTable = "Exams"
    var id = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_exam)

        try {
            val titleEnter: EditText = findViewById(R.id.TitleEnter)
            //val dateEnter: EditText = findViewById(R.id.ExamDateEnter)
            val typeEnter: EditText = findViewById(R.id.ExamTypeEnter)
            val dateEnter: CalendarView = findViewById(R.id.calendar)
            var calendarView: CalendarView = findViewById(R.id.calendar)
            var dateView: TextView = findViewById(R.id.dateView)

            //take the date
            calendarView.setOnDateChangeListener(CalendarView.OnDateChangeListener { calendarView: CalendarView, i: Int, i1: Int, i2: Int ->
                val date = i2.toString() + "−" + (i1 + 1) + "−" + i
                dateView.text = date
            })

            val bundle: Bundle? = intent.extras
            id = bundle!!.getInt("ID", 0)
            if (id != 0) {
                titleEnter.setText(bundle.getString("Lecture"))
                dateView.text = bundle.getString("ExamType")
                typeEnter.setText(bundle.getString("ExamTime"))
            }
        } catch (ex: Exception) {
        }
    }

    fun addFun(view: View) {
        val titleEnter: EditText = findViewById(R.id.TitleEnter)
        val dateEnter: TextView = findViewById(R.id.dateView)
        val typeEnter: EditText = findViewById(R.id.ExamTypeEnter)

        var dbManager = DatabaseManager(this)
        var values = ContentValues()
        values.put("Lecture", titleEnter.text.toString())
        values.put("ExamTime", dateEnter.text.toString())
        values.put("ExamType", typeEnter.text.toString())

        if (id == 0) {
            val ID = dbManager.insert(values)
            if (ID > 0) {
                Toast.makeText(this, "Exam Added", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Error: Exam not Added", Toast.LENGTH_SHORT).show()

            }
        } else {
            var selectionArgs = arrayOf(id.toString())
            val id = dbManager.update(values, "ID=?", selectionArgs)
            if (id > 0) {
                Toast.makeText(this, "Exam Updated", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Error: Exam not Updated", Toast.LENGTH_SHORT).show()

            }
        }

    }
}