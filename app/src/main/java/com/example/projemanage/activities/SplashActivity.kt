package com.example.projemanage.activities

import android.content.Intent
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import com.example.projemanage.R
import com.example.projemanage.firebase.FireStoreClass
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

//         This is used to hide the status bar and make the splash screen as a full screen activity.
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // This is used to get the file from the assets folder and set it to the title textView.
        val typeface: Typeface =Typeface.createFromAsset(assets, "Sweets Smile.ttf")
        tv_app_name.typeface = typeface

        // Here we will launch the Intro Screen after the splash screen using the handler. As using handler the splash screen will disappear after what we give to the handler.)
        // Adding the handler to after the a task after some delay.
        Handler().postDelayed({
            // Start the Intro Activity

            val currentUserID= FireStoreClass().getCurrentUserID()
            val currentUser= FirebaseAuth.getInstance().currentUser

            if(currentUser!= null){
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            }
            else{
                startActivity(Intent(this@SplashActivity, IntroActivity::class.java))
            }

            finish() // Call this when your activity is done and should be closed.
        }, 2500) // Here we pass the delay time in milliSeconds after which the splash activity will disappear.
    }
}