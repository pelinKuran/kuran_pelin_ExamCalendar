package ise308.kuran.pelin.examcalendar

import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog

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
        var projections = arrayOf("ID", "Lecture", "ExamType", "ExamTime","IsStudied")
        val selectionArgs = arrayOf(title)
        val cursor = dbManager.myQuery(projections, "Lecture like ?", selectionArgs, "Lecture")
        listExams.clear()
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndex("ID"))
                val title = cursor.getString(cursor.getColumnIndex("Lecture"))
                val description = cursor.getString(cursor.getColumnIndex("ExamType"))
                val time = cursor.getString(cursor.getColumnIndex("ExamTime"))
                val bool: Boolean = cursor.getInt(cursor.getColumnIndex("IsStudied")) == 0
                listExams.add(Exam(id, title, description, time, bool))
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
        cursor.close()


    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
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

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var myView = layoutInflater.inflate(R.layout.exams, null)
            var myExam = listExamsAdapter[position]

            myView.findViewById<TextView>(R.id.titleLecture).text = myExam.examLecture
            myView.findViewById<TextView>(R.id.titleExamType).text = myExam.examType
            myView.findViewById<TextView>(R.id.examTime).text = myExam.examDay
            var deleteBtn = myView.findViewById<ImageButton>(R.id.deleteBtn)
            var editBtn = myView.findViewById<ImageButton>(R.id.editBtn)
            //delete button
            deleteBtn.setOnClickListener {
                var dbManager = DatabaseManager(this.context!!)
                val alertDialog = AlertDialog.Builder(context!!)
                    .setTitle("Warning")
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
                var dbManager = DatabaseManager(this.context!!)
                val inflater = LayoutInflater.from(context)
                val view = inflater.inflate(R.layout.exams_update,null)
                val editLecture : TextView = view.findViewById(R.id.editLecture)
                val editType : TextView = view.findViewById(R.id.editType)
                val editDate: TextView = view.findViewById(R.id.editDate)

                editLecture.text = myExam.examLecture
                editType.text= myExam.examType
                editDate.text= myExam.examDay

                val builder = AlertDialog.Builder(context!!)
                    .setTitle("Update Customer Info")
                    .setView(view)
                    .setPositiveButton("Update", DialogInterface.OnClickListener { dialog, which ->
          //NO VALUE PASSED FOR PARAMETER isSTUDIED
                        val isUpdate: Boolean = dbManager.editUpdate(myExam.examID.toString(), editLecture.text.toString(),editDate.text.toString(),editType.text.toString(),true)
                    if(isUpdate){
                        myExam.examLecture = editLecture.text.toString()
                        myExam.examType = editType.text.toString()
                        myExam.examDay = editDate.text.toString()
                        notifyDataSetChanged()
                        Toast.makeText(context, "Update Successful", Toast.LENGTH_SHORT).show()

                    }
                        else{
                        Toast.makeText(context, "Error Updating", Toast.LENGTH_SHORT).show()
                    }
                    }).setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->  })
                val alert = builder.create()
                alert.show()
                        //updateFun(myExam)

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

    private fun updateFun(myExam: Exam) {
        var intent = Intent(this, AddExamActivity::class.java)
        intent.putExtra("ID", myExam.examID) // put id
        intent.putExtra("Lecture", myExam.examLecture) //put name
        intent.putExtra("ExamType", myExam.examType) //put type
        intent.putExtra("ExamTime", myExam.examDay) // put time
        startActivity(intent) // start activity
    }
}