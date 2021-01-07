package com.example.diplomaapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.annotation.GlideModule
import org.json.JSONException
import org.json.JSONObject


class PreviewMessage : AppCompatActivity() {

    private var mQueue: RequestQueue? = null
    private lateinit var imageView: ImageView
    private lateinit var messageView : TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview_message)

        supportActionBar!!.title = "Message"

        mQueue = Volley.newRequestQueue(this)
        var userName = intent.getStringExtra("UserName")
        var recipient = intent.getStringExtra("Recipient")

        val url = "http://192.168.0.142/get-message"
        var photoJson: JSONObject = JSONObject()
        photoJson.put("UserName",userName)
        photoJson.put("Recipient",recipient)

        var photoID = ""
        var message = ""
        val request =
            JsonObjectRequest(
                Request.Method.POST, url, photoJson,
                Response.Listener { response ->
                    try {
                        photoID = response.getString("PhotoID")
                        message = response.getString("Message")
                        val photoURL = "http://192.168.0.142/get-photo/" + photoID;
                        imageView = findViewById(R.id.imageView)
                        messageView = findViewById(R.id.et_Message)
                        Glide.with(this).load(photoURL).into(imageView);
                        messageView.setText(message)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        Log.d("VOLLEY",e.printStackTrace().toString())
                    }
                }, Response.ErrorListener { error -> error.printStackTrace() })

        mQueue!!.add(request)

    }
}

