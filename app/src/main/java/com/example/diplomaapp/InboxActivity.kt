package com.example.diplomaapp

import android.app.Dialog
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject

class InboxActivity : AppCompatActivity() {

    private var mQueue: RequestQueue? = null
    private var userName = ""
    private var title = ""


    private var previewMessage: Boolean = false
    private var isAdmin: Boolean = false
    private var showSendMessageIcon: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inbox)

        var bundle = intent.extras
        previewMessage = bundle!!.getBoolean("PreviewMessage")
        isAdmin = bundle!!.getBoolean("Admin")

        userName = intent.getStringExtra("UserName")
        title = intent.getStringExtra("Title")

        if ( title != "" )
            supportActionBar!!.title = title
        else {
            supportActionBar!!.title = "Messages " + userName
        }

        val messagesList = findViewById<ListView>(R.id.messagesListView) as ListView

        var messages = intent?.getParcelableArrayListExtra<Message>("UserList")

        val customAdapter = messages?.let { CustomAdapter(this, it, supportFragmentManager, applicationContext, contentResolver, userName, previewMessage, isAdmin ) }
        messagesList.adapter = customAdapter

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        var menuInflater = getMenuInflater()
        menuInflater.inflate(R.menu.messages_view_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settings -> Toast.makeText(this, "Selected Settings", Toast.LENGTH_SHORT).show()
            R.id.userPanel -> openUserPanel()
            R.id.camera -> openCameraApp()
        }
        return true
    }

    private fun openUserPanel(){
        if (!isAdmin) {
            var userNameData: JSONObject = JSONObject()
            userNameData.put("UserName", userName)
            mQueue = Volley.newRequestQueue(this)
            val url = "http://192.168.0.142/get-user-data"
            val request =
                JsonObjectRequest(
                    Request.Method.POST, url, userNameData,
                    Response.Listener { response ->
                        try {
                            val firstName = response.getString("FirstName")
                            val lastName = response.getString("LastName")
                            val registrationDate = response.getString("RegistrationDate")
                            val intent = Intent(this@InboxActivity, UserPanel::class.java)
                            intent.putExtra("UserName", userName)
                            intent.putExtra("FirstName", firstName)
                            intent.putExtra("LastName", lastName)
                            intent.putExtra("RegistrationDate", registrationDate)
                            startActivity(intent)

                        } catch (e: JSONException) {
                            e.printStackTrace()
                            Log.d("VOLLEY", e.printStackTrace().toString())
                        }
                    }, Response.ErrorListener { error -> error.printStackTrace() })
            mQueue!!.add(request)
        }
    }

    private fun openCameraApp(){
        var bundle = intent.extras
        var showSendMessageIcon = bundle!!.getBoolean("SendMessageIcon")
        if (showSendMessageIcon) {
            val url = "http://192.168.0.142/get-users"
            mQueue = Volley.newRequestQueue(this)
            val messages = ArrayList<Message>()
            val request = JsonArrayRequest(
                Request.Method.POST, url, null, Response.Listener { response ->
                val jsonOutput = response.length()
                for (i in 0 until response.length()) {
                    val username = response.getJSONObject(i).getString("UserName")
                    val firstName = response.getJSONObject(i).getString("FirstName")
                    val lastName = response.getJSONObject(i).getString("LastName")
                    val lastLogin = response.getJSONObject(i).getString("LastLogin")
                    messages.add(Message(R.drawable.walach, username, firstName, lastName, lastLogin, "", ""))
                }


                val intent = Intent(this@InboxActivity, InboxActivity::class.java)
                intent.putParcelableArrayListExtra("UserList",messages)
                    intent.putExtra("UserName", userName)
                intent.putExtra("SendMessageIcon",false)
                    intent.putExtra("PreviewMessage", false)
                    intent.putExtra("Title", "Choose user")
                    intent.putExtra("Admin", false)
                startActivity(intent)
            },
                Response.ErrorListener { error ->
                    Log.d("ERROR", error.toString())
                })

            // Add request to Queue
            mQueue!!.add(request)
        }
    }

    class CustomAdapter(private var myContext: Context, private var messages: ArrayList<Message>, private var mySupportFragmentManager: FragmentManager, private var applicationContext: Context, private var contentResolver: ContentResolver, private var userName : String, private var previewMessage: Boolean, private var isAdmin: Boolean) : BaseAdapter() {

        override fun getCount(): Int {
            return messages.size
        }

        override fun getItem(position: Int): Any {
            return messages[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, view: View?, viewGroup: ViewGroup): View {
            var myView = view
            var helper = MyHelper(applicationContext)
            var db: SQLiteDatabase = helper.readableDatabase

            val message = this.getItem(position) as Message
            var recipient = message.getPersonName()



            // To avoid warning - only if myView is not null - create new one.
            if (myView == null)
                myView = LayoutInflater.from(myContext).inflate(R.layout.custom_list_layout, viewGroup, false)

            val imageView = myView!!.findViewById<ImageView>(R.id.personAvatar)
            val personName = myView.findViewById<TextView>(R.id.personName)
            val personMessage = myView.findViewById<TextView>(R.id.personMessage)
            val messageDateReceived = myView.findViewById<TextView>(R.id.messageDateReceived)

            imageView.setImageResource(message.getAvatar())
            personName.text = message.getPersonName()
            if (previewMessage) {
                personMessage.text = message.getPersonMessage()
                messageDateReceived.text = message.getMessageDate()
            } else {
                personMessage.text = message.getFirstName() + " " + message.getLastName()
                messageDateReceived.text = "Last online: " + message.getLastLogin()
            }

            myView.setOnClickListener {
                if(!isAdmin) {
                    if (!previewMessage) {
                        //Toast.makeText(myContext, message.getPersonMessage(), Toast.LENGTH_SHORT).show()
                        val args = Bundle()
                        args.putString("Sender", userName)
                        args.putString("Recipient", recipient)
                        val dialog = MyDialog()
                        dialog.setArguments(args)
                        dialog.show(mySupportFragmentManager, "Dialog")
                    } else {
                        val intent = Intent(myContext, PreviewMessage::class.java)
                        intent.putExtra("UserName", userName)
                        intent.putExtra("Recipient", recipient)
                        myContext.startActivity(intent)
                    }
                } else {
                    val personUserName = message.getPersonName()
                    val firstName = message.getFirstName()
                    val lastName = message.getLastName()
                    val loginDate = message.getLastLogin()
                    val intent = Intent(myContext, AdminUserActivity::class.java)
                    intent.putExtra("UserName", personUserName)
                    intent.putExtra("FirstName", firstName)
                    intent.putExtra("LastName", lastName)
                    intent.putExtra("LoginDate", loginDate)
                    myContext.startActivity(intent)
                }

            }
            return myView
        }
    }

    class MyDialog : DialogFragment() {

        private var message: String? = null
        private var sender: String? = null
        private var recipient: String? = null

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            sender = arguments?.getString("Sender")
            recipient = arguments?.getString("Recipient")
            return super.onCreateDialog(savedInstanceState)
        }


        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            var rs = context?.contentResolver?.query(DraftProviders.CONTENT_URI, arrayOf(DraftProviders.MESSAGE,DraftProviders.SENDER, DraftProviders.RECIPIENT),"SENDER='$sender' AND RECIPIENT='$recipient'",null,null)
            if(rs?.moveToNext() !! ) {
                message = rs.getString(0)
                if (message != null)
                    Toast.makeText(context, "Read from drafts...", Toast.LENGTH_SHORT).show()
            }
            var myView = inflater.inflate(R.layout.write_message, container, false)
            var saveToDraftButton = myView.findViewById<Button>(R.id.saveToDraft)
            var selectPhoto = myView.findViewById<Button>(R.id.selectPhoto)
            var writeMessage = myView.findViewById<EditText>(R.id.et_writeMessage)
            writeMessage.setText(message)
            saveToDraftButton.setOnClickListener {
                context?.contentResolver?.delete(DraftProviders.CONTENT_URI, "SENDER='$sender' AND RECIPIENT='$recipient'", null)
                var cv = ContentValues()
                cv.put(DraftProviders.SENDER,sender)
                cv.put(DraftProviders.RECIPIENT,recipient)
                cv.put(DraftProviders.MESSAGE,writeMessage.text.toString())
                Log.d("CHECKING..", writeMessage.text.toString())
                context?.contentResolver?.insert(DraftProviders.CONTENT_URI, cv)
                Toast.makeText(context, "Saved for future use...", Toast.LENGTH_SHORT).show()
            }

            selectPhoto.setOnClickListener {
                val intent = Intent(this.context, TakePhotoActivity::class.java)
                intent.putExtra("Sender", sender)
                intent.putExtra("Recipient", recipient)
                intent.putExtra("Message", message)
                startActivity(intent)
            }

            getDialog()!!.getWindow()
            return myView
        }

        override fun onStart() {
            super.onStart()
            val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
            val height = (resources.displayMetrics.heightPixels * 0.40).toInt()
            dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    /*
    class MyDialogAdmin : DialogFragment() {

        private var userName: String? = null
        private var firstName: String? = null
        private var lastName: String? = null
        private var lastLogin: String? = null

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            userName = arguments?.getString("UserName")
            firstName = arguments?.getString("FirstName")
            lastName = arguments?.getString("LastName")
            lastLogin = arguments?.getString("LastLogin")
            return super.onCreateDialog(savedInstanceState)
        }


        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            var myView = inflater.inflate(R.layout.user_details, container, false)
            var userNameTextView = myView.findViewById<TextView>(R.id.tv_userName)
            var firstNameTextView = myView.findViewById<TextView>(R.id.tv_First_Name)
            var lastNameTextView = myView.findViewById<TextView>(R.id.tv_Last_Name)
            var lastLoginTextView = myView.findViewById<TextView>(R.id.tv_Last_Login)
            var removeButton = myView.findViewById<Button>(R.id.removeButton)

            userNameTextView.setText("User Name: " + userName)
            firstNameTextView.setText("First Name: " + firstName)
            lastNameTextView.setText("Last Name: " + lastName)
            lastLoginTextView.setText("Last Login: " + lastLogin)
            getDialog()!!.getWindow()
            return myView
        }

        override fun onStart() {
            super.onStart()
            val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
            val height = (resources.displayMetrics.heightPixels * 0.40).toInt()
            dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

     */
}
