package com.example.projemanage
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.projemanage.activities.BaseActivity
import com.example.projemanage.firebase.FireStoreClass
import com.example.projemanage.model.User
import com.example.projemanage.utils.Constants
import com.example.projemanage.utils.Constants.showImageChooser
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_my_profile.*
import java.io.IOException

class MyProfileActivity : BaseActivity() {


    private var mSelectedImageFileUri: Uri?= null // Add a global variable for URI of a selected image from phone storage.
    private var mProfileImageURL: String = "" // A global variable for a user profile image URL
    private lateinit var mUserDetails: User // A global variable for user details.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)

        setUpActionbar()

        FireStoreClass().loadUserData(this@MyProfileActivity)

        iv_profile_user_image.setOnClickListener {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            {
                Constants.showImageChooser(this@MyProfileActivity)
            }else{
                ActivityCompat.requestPermissions(this@MyProfileActivity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), Constants.READ_STORAGE_PERMISSION_CODE)
            }
        }

        btn_update.setOnClickListener {

            if(mSelectedImageFileUri!= null){ // Here if the image is not selected then update the other details of user.
                uploadUserImage()
            }else{
                showProgressDialog(resources.getString(R.string.please_wait))
                updateUserProfileData()
            }
        }
    }

    private fun setUpActionbar(){
        setSupportActionBar(toolbar_my_profile_activity)
        val actionBar= supportActionBar
        if(actionBar!= null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title= resources.getString(R.string.my_profile)
        }

        toolbar_my_profile_activity.setNavigationOnClickListener { onBackPressed() }
    }

    fun setUserDataUI(user: User){

        mUserDetails= user

        Glide
            .with(this@MyProfileActivity)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(iv_profile_user_image)

        et_name.setText(user.name)
        et_email.setText(user.email)
        if(user.mobile!= 0L){
            et_mobile.setText(user.mobile.toString())
        }

    }

    //This function will identify the result of runtime permission after the user allows or deny permission based on the unique code.
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode== Constants.READ_STORAGE_PERMISSION_CODE)
        {
            if(grantResults.isNotEmpty() && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                showImageChooser(this@MyProfileActivity)
            }else{
                Toast.makeText(applicationContext, "Oops!, you just denied the permission for storage. You can allow from settings.", Toast.LENGTH_LONG).show()
            }
        }

    }


    // Get the result of the image selection based on the constant code.
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode== Activity.RESULT_OK && requestCode== Constants.PICK_IMAGE_REQUEST_CODE && data!!.data!= null)
        {
            mSelectedImageFileUri= data.data

            try {
                Glide
                    .with(this@MyProfileActivity)
                    .load(mSelectedImageFileUri)
                    .centerCrop()
                    .placeholder(R.drawable.ic_user_place_holder)
                    .into(iv_profile_user_image)
            }catch (e: IOException){
                e.printStackTrace()
            }

        }
    }

    //  A function to upload the selected user image to firebase cloud storage.

    private fun uploadUserImage(){
        showProgressDialog(resources.getString(R.string.please_wait))

        if(mSelectedImageFileUri!=null){

            //getting the storage reference
            val sRef: StorageReference= FirebaseStorage.getInstance().reference.child(
                "USER_IMAGE" + System.currentTimeMillis() + "." + Constants.getFileExtension(this,
                    mSelectedImageFileUri!!
                )
            )

            //adding the file to reference
            sRef.putFile(mSelectedImageFileUri!!)
                .addOnSuccessListener { taskSnapshot ->
                    // The image upload is success
                    Log.e("Firebase Image URL", taskSnapshot.metadata!!.reference!!.downloadUrl.toString())

                    // Get the downloadable url from the task snapshot
                    taskSnapshot.metadata!!.reference!!.downloadUrl
                        .addOnSuccessListener { uri ->
                            Log.e("Downloadable Image URL", uri.toString())

                            // assign the image url to the variable.
                            mProfileImageURL = uri.toString()

                            // Call a function to update user details in the database.
                            updateUserProfileData()
                        }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this@MyProfileActivity, exception.message, Toast.LENGTH_LONG).show()
                    hideProgressDialog()
                }
        }
    }

    // A function to update the user profile details into the database.

    private  fun updateUserProfileData(){
        val userHashMap= HashMap<String, Any>()

        if(mProfileImageURL.isNotEmpty() && mProfileImageURL!= mUserDetails.image){
            userHashMap[Constants.IMAGE]= mProfileImageURL
        }

        if(et_name.text.toString() != mUserDetails.name){
            userHashMap[Constants.NAME]= et_name.text.toString()
        }

        if(et_mobile.text.toString() != mUserDetails.mobile.toString()){
            userHashMap[Constants.MOBILE]= et_mobile.text.toString().toLong()
        }

        FireStoreClass().updateUserProfileData(this@MyProfileActivity, userHashMap)
    }

    // A function to notify the user profile is updated successfully.
    fun profileUpdateSuccess() {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)// Send the success result to the Base Activity
        finish()
    }
}