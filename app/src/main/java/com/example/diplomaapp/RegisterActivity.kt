package com.example.diplomaapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject

class RegisterActivity : AppCompatActivity() {

    private var mQueue: RequestQueue? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val emailAddress = findViewById<EditText>(R.id.et_Email)
        val password = findViewById<EditText>(R.id.et_Pass)
        val confPassword = findViewById<EditText>(R.id.et_confirmPass)
        val firstName = findViewById<EditText>(R.id.et_FirstName)
        val lastName = findViewById<EditText>(R.id.et_LastName)
        val registerButton = findViewById<Button>(R.id.registerButton)

        var emailAddressText = ""
        var passwordText = ""
        var confPasswordText = ""
        var firstNameText = ""
        var lastNameText = ""

        val url = "http://192.168.0.142/register"
        mQueue = Volley.newRequestQueue(this)

        registerButton.setOnClickListener {
            emailAddressText = emailAddress.text.toString()
            passwordText = password.text.toString()
            confPasswordText = confPassword.text.toString()
            firstNameText = firstName.text.toString()
            lastNameText = lastName.text.toString()

            if (emailAddress.length() == 0 || password.length() < 8 || confPasswordText != passwordText || firstName.length() == 0 || lastName.length() == 0)
                Toast.makeText(this, "Wrong input!", Toast.LENGTH_SHORT).show()
            else {
                var registerData: JSONObject = JSONObject()
                registerData.put("UserName",emailAddressText)
                registerData.put("Password",passwordText)
                registerData.put("FirstName",firstNameText)
                registerData.put("LastName",lastNameText)

                // Request for JSON
                val request =
                    JsonObjectRequest(
                        Request.Method.POST, url, registerData,
                        Response.Listener { response ->
                            try {
                                val jsonOutput = response.getString("Status")
                                Log.d("VOLLEY",jsonOutput)
                                if (jsonOutput == "Passed."){
                                    Toast.makeText(this, "Successful creation of user, now you can log in.",Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                                    }
                                else
                                    Toast.makeText(this, "Wrong password or username.", Toast.LENGTH_SHORT).show()
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
}
