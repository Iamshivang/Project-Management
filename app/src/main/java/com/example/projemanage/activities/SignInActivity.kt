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
import kotlinx.android.synthetic.main.activity_sign_in.*

class SignInActivity : BaseActivity() {

    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        auth= FirebaseAuth.getInstance()

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setUpActionBar()

        btn_sign_in.setOnClickListener {
            signInRegisteredUser()
        }
    }

    fun signedInSuccess(user: User)
    {
        hideProgressDialog()
        startActivity(Intent(this@SignInActivity, MainActivity::class.java))
        finish()
    }

    fun signedInFail()
    {
        hideProgressDialog()
        Toast.makeText(this@SignInActivity, "Sign In Failed", Toast.LENGTH_SHORT).show()
    }

    private fun setUpActionBar()
    {
        setSupportActionBar(toolbar_sign_in_activity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        toolbar_sign_in_activity.setNavigationOnClickListener{ onBackPressed()}
    }

    private fun signInRegisteredUser()
    {
        val email: String= et_email_sign_in.text.toString().trim { it <= ' '}
        val password: String= et_password_sign_in.text.toString().trim { it <= ' '}

        if(validateForm(email, password))
        {
            showProgressDialog(resources.getString(R.string.please_wait))

            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this@SignInActivity){task ->

                if (task.isSuccessful)
                {
                    // SIgn in success, update UI with the signed in user's information

                    // FOR FIREBASE AUTHENTICATION
//                    Log.d("Sign in", "signInWithEmail: Success")
//                    val user= auth.currentUser
//                    startActivity(Intent(this@SignInActivity, MainActivity::class.java))

                    // HERE WE GOING FOR FIREBASE DATABASE
                    // Calling the FireStoreClass signInUser function to get the data of user from database.
                    FireStoreClass().loadUserData(this@SignInActivity)
                }else{
                    // if sign fails, display a message to the user.
                    Log.w("Sign in", "signInWithEmail: failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                    hideProgressDialog()
                }
            }

        }
    }

    private fun validateForm(email: String, password: String): Boolean{
        return when{

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