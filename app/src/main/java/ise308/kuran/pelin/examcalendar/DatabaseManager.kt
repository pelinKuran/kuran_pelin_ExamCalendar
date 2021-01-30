package ise308.kuran.pelin.examcalendar


import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteQueryBuilder
import android.util.Log
import java.lang.Exception

class DatabaseManager(context: Context) {
    //db name
    var dbname = "ExamDates"

    //db table
    var dbTable = "Exams"
    var colID = "ID"
    var colTitle = "Lecture"
    var colType = "ExamType"
    var colTime = "ExamTime"
    var colBool = "IsStudied"


    //version
    var dbVersion = 1

    //create table
    val createTableSQL =
        "CREATE TABLE IF NOT EXISTS " + dbTable + "(" + colID + " INTEGER PRIMARY KEY, " + colTitle + " TEXT, " + colType + " VARCHAR, " + colTime + " VARCHAR, " + colBool + " INTEGER DEFAULT 0);"
    var sqlDB: SQLiteDatabase? = null

    init {
        val db = DatabaseHelper(context)
        sqlDB = db.writableDatabase
    }

    inner class DatabaseHelper(context: Context) :
        SQLiteOpenHelper(context, dbname, null, dbVersion) {
        //onCreate will be called when the tables are created for the first time. After executing the first time method will not be called again.
        override fun onCreate(p0: SQLiteDatabase?) {
            p0!!.execSQL(createTableSQL)
        }

        //onUpgrade is called when db version upgraded. Version uprades as db upgraded.
        override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
            p0!!.execSQL("Drop table if Exists$dbTable")

        }
    }

    fun insert(values: ContentValues): Long {
        var inserted = sqlDB!!.insert(dbTable, "", values)
        sqlDB!!.close()
        return inserted
    }

    fun myQuery(
        projection: Array<String>,
        selection: String,
        selectionArgs: Array<String>,
        sOrder: String
    ): Cursor {
        val db = SQLiteQueryBuilder();
        db.tables = dbTable

        return db.query(sqlDB, projection, selection, selectionArgs, null, null, sOrder)

    }

    fun delete(selection: String, selectionArgs: Array<String>): Int {
        var deleted = sqlDB!!.delete(dbTable, selection, selectionArgs)
        sqlDB!!.close()
        return deleted
    }

    fun update(values: ContentValues, selection: String, selectionArgs: Array<String>): Int {
        var updated = sqlDB!!.update(dbTable, values, selection, selectionArgs)
        sqlDB!!.close()
        return updated
    }

    fun editUpdate(
        id: String,
        lecture: String,
        examDate: String,
        examType: String,
        isStudied: Boolean

    ): Boolean {
        val myDb = this.sqlDB
        val values = ContentValues()
        var flag = false
        values.put(colTitle, lecture)
        values.put(colTime, examDate)
        values.put(colType, examType)
        values.put(colBool, isStudied)

        try {

            myDb?.update(dbTable, values, "$colID = ?", arrayOf(id))
            flag = true

        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "Error Updating")
        }
        return flag
    }

}