package ise308.kuran.pelin.examcalendar

import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //load from db
        loadQuery("%")
    }

    var listExams = ArrayList<Exam>()
    override fun onResume() {
        super.onResume()
        loadQuery("%")
    }

    private fun loadQuery(title: String) {
        var dbManager = DatabaseManager(this)
        var boolIsStudied = false
        var projections = arrayOf("ID", "Lecture", "ExamType", "ExamTime", "IsStudied")
        val selectionArgs = arrayOf(title)
        val cursor = dbManager.myQuery(projections, "Lecture like ?", selectionArgs, "Lecture")
        listExams.clear()
        //put values into listExams
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndex("ID"))
                val title = cursor.getString(cursor.getColumnIndex("Lecture"))
                val description = cursor.getString(cursor.getColumnIndex("ExamType"))
                val time = cursor.getString(cursor.getColumnIndex("ExamTime"))
                var isStudied: Int = cursor.getInt(cursor.getColumnIndex("IsStudied"))
                if (isStudied == 1) boolIsStudied = true
                listExams.add(Exam(id, title, description, time, boolIsStudied))
            } while (cursor.moveToNext())
        }

        //adapter

        var myExamsAdapter = MyExamsAdapter(this, listExams)
        //set adapter
        var examList: ListView = findViewById(R.id.examList)
        examList.adapter = myExamsAdapter
        // get count from LV
        val total = examList.count
        //actionbar
        val mActionBar = supportActionBar
        if (mActionBar != null) {
            //set subtitle
            mActionBar.subtitle = "You have " + total + " exam(s)."
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        val searchView: SearchView = menu!!.findItem(R.id.app_search_bar).actionView as SearchView
        val sm = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView.setSearchableInfo(sm.getSearchableInfo(componentName))
        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(p0: String?): Boolean {
                loadQuery ("%$p0%")
                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                loadQuery ("%$p0%")
                return false
            }
        });
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item != null) {
            when (item.itemId) {
                R.id.addExam -> {
                    startActivity(Intent(this, AddExamActivity::class.java))
                }

            }
        }
        return super.onOptionsItemSelected(item)
    }

    inner class MyExamsAdapter : BaseAdapter {
        var listExamsAdapter = ArrayList<Exam>()
        var context: Context? = null

        constructor(context: Context, listExamsAdapter: ArrayList<Exam>) : super() {
            this.listExamsAdapter = listExamsAdapter
            this.context = context
        }

        @SuppressLint("ResourceAsColor")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var dbManager = DatabaseManager(this.context!!)
            var myView = layoutInflater.inflate(R.layout.exams, null)
            var cardView = myView.findViewById<CardView>(R.id.cardView)
            var myExam = listExamsAdapter[position]
            myView.findViewById<TextView>(R.id.titleLecture).text = myExam.examLecture
            myView.findViewById<TextView>(R.id.titleExamType).text = myExam.examType
            myView.findViewById<TextView>(R.id.examTime).text = myExam.examDay
            var deleteBtn = myView.findViewById<ImageButton>(R.id.deleteBtn)
            var editBtn = myView.findViewById<ImageButton>(R.id.editBtn)
            val title = myView.findViewById<TextView>(R.id.titleLecture).text.toString()
            val type = myView.findViewById<TextView>(R.id.titleExamType).text.toString()
            val time = myView.findViewById<TextView>(R.id.examTime).text.toString()
            val s = "Lecture: " + title + "\n" + "Exam: " + type + "\n" + "Date: " + time
            if (myExam.boolean!!) {
                cardView.setCardBackgroundColor(R.color.black)
            }
            //delete button click
            deleteBtn.setOnClickListener {
                var dbManager = DatabaseManager(this.context!!)
                val alertDialog = AlertDialog.Builder(context!!)
                    .setTitle("DELETE")
                    .setMessage("Are You Sure?")
                    .setPositiveButton(
                        "Yes",
                        DialogInterface.OnClickListener { dialogInterface: DialogInterface, i: Int ->
                            val selectionArgs = arrayOf(myExam.examID.toString())
                            dbManager.delete("ID=?", selectionArgs)
                            loadQuery("%")
                        })
                    .setNegativeButton("No", DialogInterface.OnClickListener { dialog, which -> })
                    .setIcon(R.drawable.ic_action_warning)
                    .show()
            }
            //edit//update button click
            editBtn.setOnClickListener {
                val inflater = LayoutInflater.from(context)
                val view = inflater.inflate(R.layout.exams_update, null)
                val editLecture: TextView = view.findViewById(R.id.editLecture)
                val editType: TextView = view.findViewById(R.id.editType)
                val editDate: TextView = view.findViewById(R.id.editDate)
                val sw1: CheckBox = view.findViewById(R.id.isStudied)
                editLecture.text = myExam.examLecture
                editType.text = myExam.examType
                editDate.text = myExam.examDay
                val builder = AlertDialog.Builder(context!!)
                    .setTitle("UPDATE EXAM")
                    .setView(view)
                    .setPositiveButton("Update", DialogInterface.OnClickListener { dialog, which ->
                        val isUpdate: Boolean = dbManager.editUpdate(
                            myExam.examID.toString(),
                            editLecture.text.toString(),
                            editDate.text.toString(),
                            editType.text.toString(),
                            sw1.isChecked
                        )
                        if (isUpdate) {

                            myExam.examLecture = editLecture.text.toString()
                            myExam.examType = editType.text.toString()
                            myExam.examDay = editDate.text.toString()
                            myExam.boolean = sw1.isChecked
                            notifyDataSetChanged()
                            Toast.makeText(context, "Update Successful", Toast.LENGTH_SHORT).show()

                        } else {
                            Toast.makeText(context, "Error Updating", Toast.LENGTH_SHORT).show()
                        }
                    }).setNegativeButton(
                        "Cancel",
                        DialogInterface.OnClickListener { dialog, which -> })
                val alert = builder.create()
                alert.show()
            }
            //copy button click
            myView.findViewById<ImageButton>(R.id.copyButton).setOnClickListener {

                val copy = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                copy.text = s // added to clipboard
                Toast.makeText(this@MainActivity, "Copied", Toast.LENGTH_SHORT).show()
            }
            //share button click
            myView.findViewById<ImageButton>(R.id.shareButton).setOnClickListener {
                val shareIntent = Intent()
                shareIntent.action = Intent.ACTION_SEND
                shareIntent.type = "text/plain"
                shareIntent.putExtra(Intent.EXTRA_TEXT, s)
                startActivity(Intent.createChooser(shareIntent, s))

            }
            return myView
        }
        override fun getItem(position: Int): Any {
            return listExamsAdapter[position]
        }
        override fun getItemId(position: Int): Long {
            return position.toLong()
        }
        override fun getCount(): Int {
            return listExamsAdapter.size
        }
    }

}