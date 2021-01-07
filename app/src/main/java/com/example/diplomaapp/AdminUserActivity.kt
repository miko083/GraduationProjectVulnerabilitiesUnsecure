package com.example.diplomaapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject

class AdminUserActivity : AppCompatActivity() {
    private var mQueue: RequestQueue? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_user)

        val userNameTextView = findViewById<TextView>(R.id.userName)
        val firstNameTextView = findViewById<TextView>(R.id.firstName)
        val lastNameTextView = findViewById<TextView>(R.id.lastName)
        val loginDateTextView = findViewById<TextView>(R.id.registrationDate)

        supportActionBar!!.title = "User details"

        val userName = intent.getStringExtra("UserName")
        userNameTextView.setText("User Name: " + userName)
        firstNameTextView.setText("First Name: " + intent.getStringExtra("FirstName"))
        lastNameTextView.setText("Last Name: " + intent.getStringExtra("LastName"))
        loginDateTextView.setText("Last Login Date: " + intent.getStringExtra("LoginDate"))

        val removeUserButton = findViewById<Button>(R.id.removeUser)
        removeUserButton.setOnClickListener {
            var userToDelete: JSONObject = JSONObject()
            userToDelete.put("username",userName)

            val url = "http://192.168.0.142/delete-user"
            mQueue = Volley.newRequestQueue(this)
            val request =
                JsonObjectRequest(
                    Request.Method.POST, url, userToDelete,
                    Response.Listener { response ->
                        try {
                            val jsonOutput = response.getString("Status")
                            if (jsonOutput == "Passed.")
                                Toast.makeText(this, "Success.", Toast.LENGTH_SHORT).show()
                            else
                                Toast.makeText(this, "Something wrong..", Toast.LENGTH_SHORT).show()
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            Log.d("VOLLEY",e.printStackTrace().toString())
                        }
                    }, Response.ErrorListener { error -> error.printStackTrace() })
            // Add request to Queue
            mQueue!!.add(request)
        }
    }
}
