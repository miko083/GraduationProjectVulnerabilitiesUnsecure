package com.example.diplomaapp

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri

class DraftProviders: ContentProvider() {

    companion object{
        // PROVIDERS Details
        val PROVIDER_NAME = "com.example.diplomaapp/DraftsProviders"
        val URL = "content://$PROVIDER_NAME/DRAFT_TABLE"
        val CONTENT_URI = Uri.parse(URL)

        // VALUES FOR DATABASES
        val MESSAGE = "MESSAGE"
        val SENDER = "SENDER"
        val RECIPIENT = "RECIPIENT"

    }
    lateinit var db : SQLiteDatabase
    override fun insert(uri: Uri, cv: ContentValues?): Uri? {
        db.insert("DRAFT_TABLE",null,cv)
        context?.contentResolver?.notifyChange(uri,null)
        return uri
    }

    override fun query(
        uri: Uri,
        cols: Array<out String>?,
        condition: String?,
        condition_val: Array<out String>?,
        order: String?
    ): Cursor? {
        return db.query("DRAFT_TABLE", cols, condition, condition_val,null,null,order)
    }

    override fun onCreate(): Boolean {
        var helper = MyHelper(context)
        db = helper.writableDatabase
        return if(db==null) false else true
    }

    override fun update(
        uri: Uri,
        cv: ContentValues?,
        condition: String?,
        condition_val: Array<out String>?
    ): Int {
        var count = db.update("DRAFT_TABLE", cv, condition, condition_val)
        context?.contentResolver?.notifyChange(uri,null)
        return count
    }

    override fun delete(uri: Uri, condition: String?, condition_val: Array<out String>?): Int {
        var count = db.delete("DRAFT_TABLE",condition,condition_val)
        context?.contentResolver?.notifyChange(uri,null)
        return count
    }

    override fun getType(uri: Uri): String? {
        return "com.android.cursor.dir/com.example.draftable"
    }
}