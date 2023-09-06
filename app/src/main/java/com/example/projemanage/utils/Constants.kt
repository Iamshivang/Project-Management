package com.example.projemanage.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.core.app.ActivityCompat.startActivityForResult

object Constants {

    // Firebase Constants
    // This  is used for the collection name for USERS.
    const val USERS: String= "users"

    // Firebase database field names
    const val IMAGE: String= "image"
    const val NAME: String= "name"
    const val MOBILE: String= "mobile"

    // This  is used for the collection name for USERS.
    const val BOARDS: String = "boards"

    const val ASSIGNED_TO: String = "assignedTo"  // Add a field name as assignedTo which we are gonna use later on.

    // Add constant for DocumentId
    const val DOCUMENT_ID: String = "documentId"

    const val TASK_LIST: String = "taskList" // Add a new field for TaskList

    const val READ_STORAGE_PERMISSION_CODE= 1 //A unique code for asking the Read Storage Permission using this we will be check and identify in the method onRequestPermissionsResult
    const val PICK_IMAGE_REQUEST_CODE= 2 //Add a constant for image selection from phone storage

    fun showImageChooser(activity: Activity){
        val galleryIntent= Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activity.startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE) // Launches the image selection of phone storage using the constant cod
    }

    // A function to get the extension of selected image.
    fun getFileExtension(activity: Activity, uri: Uri): String?{

        // MimeTypeMap: Two-way map that maps MIME-types to file extensions and vice versa.
        // getSingleton(): Get the singleton instance of MimeTypeMap.
        // getExtensionFromMimeType: Return the registered extension for the given MIME type.
        // contentResolver.getType: Return the MIME type of the given content URL.

        return MimeTypeMap.getSingleton().getExtensionFromMimeType(activity.contentResolver.getType(uri))
    }
}