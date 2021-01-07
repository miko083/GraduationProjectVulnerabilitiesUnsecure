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
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject

class UserPanel : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_panel)

        val userNameTextView = findViewById<TextView>(R.id.userName)
        val firstNameTextView = findViewById<TextView>(R.id.firstName)
        val lastNameTextView = findViewById<TextView>(R.id.lastName)
        val registrationDateTextView = findViewById<TextView>(R.id.registrationDate)
        val changeUserNameButton = findViewById<Button>(R.id.changeUserName)

        supportActionBar!!.title = "User Panel"
        val userName = intent.getStringExtra("UserName")
        userNameTextView.setText("User Name: " + userName)
        firstNameTextView.setText("First Name: " + intent.getStringExtra("FirstName"))
        lastNameTextView.setText("Last Name: " + intent.getStringExtra("LastName"))
        registrationDateTextView.setText("Registration Date: " + intent.getStringExtra("RegistrationDate"))

        changeUserNameButton.setOnClickListener {
            val args = Bundle()
            args.putString("UserName", userName)
            val dialog = MyDialog()
            dialog.setArguments(args)
            dialog.show(supportFragmentManager, "Dialog")
        }

    }

    class MyDialog : DialogFragment() {

        private var userName: String? = null
        private var mQueue: RequestQueue? = null

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            userName = arguments?.getString("UserName")
            return super.onCreateDialog(savedInstanceState)
        }


        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            var myView = inflater.inflate(R.layout.change_username, container, false)
            var changeUserName = myView.findViewById<Button>(R.id.sendNewUserName)
            var writeNewUserName = myView.findViewById<EditText>(R.id.et_writeNewUserName)
            changeUserName.setOnClickListener {
                var userToChange: JSONObject = JSONObject()
                userToChange.put("UserName",userName)
                userToChange.put("UserNameNew",writeNewUserName.text)

                val url = "http://192.168.0.142/change-user-name"
                mQueue = Volley.newRequestQueue(context)
                val request =
                    JsonObjectRequest(
                        Request.Method.POST, url, userToChange,
                        Response.Listener { response ->
                            try {
                                val jsonOutput = response.getString("Status")
                                if (jsonOutput == "Passed.")
                                    Toast.makeText(context, "Success.", Toast.LENGTH_SHORT).show()
                                else
                                    Toast.makeText(context, "Something wrong..", Toast.LENGTH_SHORT).show()
                            } catch (e: JSONException) {
                                e.printStackTrace()
                                Log.d("VOLLEY",e.printStackTrace().toString())
                            }
                        }, Response.ErrorListener { error -> error.printStackTrace() })
                // Add request to Queue
                mQueue!!.add(request)
            }

            getDialog()!!.getWindow()
            return myView
        }
    }
}

