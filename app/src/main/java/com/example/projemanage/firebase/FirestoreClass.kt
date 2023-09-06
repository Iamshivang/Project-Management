package com.example.projemanage.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.example.projemanage.MyProfileActivity
import com.example.projemanage.R
import com.example.projemanage.activities.*
import com.example.projemanage.model.Board
import com.example.projemanage.model.User
import com.example.projemanage.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
/**
 * A custom class where we will add the operation performed for the fireStore database.
 */
class FireStoreClass{
    // Create a instance of Firebase FireStore
    private val mFireStore= FirebaseFirestore.getInstance()

     // A function to make an entry of the registered user in the fireStore database.
    fun registerUser(activity: SignUpActivity, userInfo: User){

        mFireStore.collection(Constants.USERS)
            // Document ID for users fields. Here the document it is the User ID.
            .document(getCurrentUserID())
            // Here the userInfo are Field and the SetOption is set to merge. It is for if we wants to merge
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                // Here call a function of base activity for transferring the result to it.
                activity.userRegisteredSuccess()
            }.addOnFailureListener { e-> Log.e(activity.javaClass.simpleName, "Error writing document", e) }
    }


     // A function to SignIn using firebase and get the user details from FireStore Database.
    fun loadUserData(activity: Activity, isToReadBoardsList: Boolean = false){

         // Here we pass the collection name from which we wants the data.
        mFireStore.collection(Constants.USERS)
            // The document id to get the Fields of user.
            .document(getCurrentUserID())
            .get()
            .addOnSuccessListener {document ->

                Log.e(activity.javaClass.simpleName, document.toString())

                // Here we have received the document snapshot which is converted into the User Data model object.
                val loggedInUser= document.toObject(User::class.java)

                when(activity){

                    is SignInActivity ->{
                        if (loggedInUser != null) {
                            // Here call a function of base activity for transferring the result to it.
                            activity.signedInSuccess(loggedInUser)
                        }
                    }

                    is MainActivity ->{
                        if (loggedInUser != null) {
                            activity.updateNavigationUserDetails(loggedInUser, isToReadBoardsList)
                        }
                    }

                    is MyProfileActivity ->{
                        if (loggedInUser != null) {
                            activity.setUserDataUI(loggedInUser)
                        }
                    }
                }

            }.addOnFailureListener { e->
                Log.e(activity.javaClass.simpleName, "Error while getting loggedIn user details", e)

                when(activity){
                    is SignInActivity ->{
                        activity.signedInFail()
                    }

                    is MainActivity ->{
                        activity.hideProgressDialog()
                        Toast.makeText(activity, "Error!", Toast.LENGTH_SHORT).show()
                    }

                    is MyProfileActivity ->{
                        activity.hideProgressDialog()
                        Toast.makeText(activity, "Error!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    fun getCurrentUserID(): String{

        val currentUser= FirebaseAuth.getInstance().currentUser
        var currentUserID: String= ""
        if(currentUser!= null)
        {
            currentUserID= currentUser.uid
        }
        return currentUserID
    }

    // A function to update the user profile data into the database.
    fun updateUserProfileData(activity: MyProfileActivity, userHashMap: HashMap<String, Any>){
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserID())
            .update(userHashMap)
            .addOnSuccessListener {

                Log.e(activity.javaClass.simpleName, "Profile Data Updated Successfully!")

                Toast.makeText(activity, "Profile updated successfully", Toast.LENGTH_LONG).show()

                activity.profileUpdateSuccess() // Notify the success result.
            }.addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while creating a board.", e)
            }
    }


    // A function to update the user profile data into the database.
    fun createBoard(activity: CreateBoardActivity, board: Board){
        mFireStore.collection(Constants.BOARDS)
            .document()
            .set(board, SetOptions.merge())
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "Board created successfully.")

                Toast.makeText(activity, "Board created successfully.", Toast.LENGTH_LONG).show()
                activity.boardCreatedSuccessfully()
            }
            .addOnFailureListener {e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while creating a board.", e)
            }
    }

    // A function to get the list of created boards from the database
    fun getBoardList(activity: MainActivity){
        mFireStore.collection(Constants.BOARDS)
            .whereArrayContains(Constants.ASSIGNED_TO, getCurrentUserID()) // A where array query as we want the list of the board in which the user is assigned. So here you can pass the current user id.
            .get()
            .addOnSuccessListener {document ->
                Log.e(activity.javaClass.simpleName, document.documents.toString())

                val boardArrayList: ArrayList<Board> = ArrayList()

                for(i in document.documents){
                    val board = i.toObject(Board::class.java)!!
                    board.documentId = i.id

                    boardArrayList.add(board)
                }
                // Here pass the result to the base activity.
                activity.populateBoardsListToUI(boardArrayList)
            }
            .addOnFailureListener { e ->

                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while creating a board.", e)
            }
    }

//    A function to get the Board Details.
    fun getBoardDetails(activity: TaskListActivity, documentId: String){
        mFireStore.collection(Constants.BOARDS)
            .document(documentId)
            .get()
            .addOnSuccessListener {document ->
                Log.e(activity.javaClass.simpleName, document.toString())

                // Assign the board document id to the Board Detail object
                val board= document.toObject(Board::class.java)!!
                board.documentId= document.id

                // Send the result of board to the base activity.
                activity.boardDetails(board)
            }
            .addOnFailureListener { e->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while creating a board.", e)
            }
    }

//    A function to create a task list in the board detail.
    fun addUpdateTaskList(activity: TaskListActivity, board: Board) {
        val taskListHashMap = HashMap<String, Any>()
        taskListHashMap[Constants.TASK_LIST] = board.taskList

        mFireStore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(taskListHashMap)
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "TaskList updated successfully.")

                activity.addUpdateTaskListSuccess()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while creating a board.", e)
            }
    }
}