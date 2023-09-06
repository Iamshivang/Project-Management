package com.example.projemanage.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import com.example.projemanage.R
import com.example.projemanage.firebase.FireStoreClass
import com.example.projemanage.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setUpActionBar()
        btn_sign_up.setOnClickListener { registerUser() }
    }

    fun userRegisteredSuccess(){
        Toast.makeText(this@SignUpActivity, "you have successfully registered the email address", Toast.LENGTH_LONG).show()
        hideProgressDialog()
//        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun setUpActionBar()
    {
        setSupportActionBar(toolbar_sign_up_activity)

        val actionBar= supportActionBar
        if(actionBar!= null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)

        }

        toolbar_sign_up_activity.setNavigationOnClickListener{ onBackPressed()}
    }

    private fun registerUser(){
        val name: String= et_name.text.toString().trim { it <= ' '}
        val email: String= et_email.text.toString().trim { it <= ' '}
        val password: String= et_password.text.toString().trim { it <= ' '}

        if(validateForm(name, email, password))
        {
            showProgressDialog(resources.getString(R.string.please_wait))
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                task ->

                if(task.isSuccessful)
                {
                    val fireBaseUser: FirebaseUser= task.result!!.user!!
                    val registeredEmail= fireBaseUser.email!!
                    // FOR FIREBASE AUTHENTICATION
//                    FirebaseAuth.getInstance().signOut()
//                    finish()

                    // HERE WE GOING FOR FIREBASE DATABASE
                    val user= User(fireBaseUser.uid, name, registeredEmail)
                    FireStoreClass().registerUser(this, user)
                }
                else{
                    Toast.makeText(this@SignUpActivity, "Registration failed.", Toast.LENGTH_LONG).show()
                    Log.e("RegistrationFailed", task.exception!!.message.toString())
                    hideProgressDialog()
                }
            }
        }
    }

    private fun validateForm(name: String, email: String, password: String): Boolean{
        return when{
            TextUtils.isEmpty(name) ->{
                showSnackBar("Please enter a name.")
                false
            }

            TextUtils.isEmpty(email) ->{
                showSnackBar("Please enter an email address.")
                false
            }

            TextUtils.isEmpty(password) ->{
                showSnackBar("Please enter a password.")
                false
            }

            else ->{
                true
            }
        }
    }
}