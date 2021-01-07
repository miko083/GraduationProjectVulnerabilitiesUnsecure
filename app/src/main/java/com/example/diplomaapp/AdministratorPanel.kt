package com.example.diplomaapp

import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.w3c.dom.Text

class AdministratorPanel : AppCompatActivity() {

    private var mQueue: RequestQueue? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_administrator_panel)
        val url = "http://192.168.0.142/get-users"
        mQueue = Volley.newRequestQueue(this)
        val userDatabaseButton = findViewById<Button>(R.id.userDatabase)

        supportActionBar!!.title = "Admin Panel"

        val messages = ArrayList<Message>()

        userDatabaseButton.setOnClickListener {
            messages.clear()
            val request = JsonArrayRequest(Request.Method.POST, url, null, Response.Listener { response ->
                for (i in 0 until response.length()) {
                    val username = response.getJSONObject(i).getString("UserName")
                    val firstName = response.getJSONObject(i).getString("FirstName")
                    val lastName = response.getJSONObject(i).getString("LastName")
                    val lastLogin = response.getJSONObject(i).getString("LastLogin")
                    messages.add(Message(R.drawable.walach, username, firstName,lastName,lastLogin ,"",""))
                }

                val intent = Intent(this@AdministratorPanel, InboxActivity::class.java)
                intent.putParcelableArrayListExtra("UserList",messages)
                // Send values to intent
                intent.putExtra("Title","List of users")
                intent.putExtra("SendMessageIcon",false)
                intent.putExtra("UserName", "")
                intent.putExtra("PreviewMessage", false)
                intent.putExtra("Title", "Choose User")
                intent.putExtra("Admin",true)
                startActivity(intent)
            },
                Response.ErrorListener { error ->
                    Log.d("ERROR", error.toString())
                })

            // Add request to Queue
            mQueue!!.add(request)

        }

    }
}
