package com.example.projemanage.activities

import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projemanage.R
import com.example.projemanage.adapters.TaskListItemsAdapter
import com.example.projemanage.firebase.FireStoreClass
import com.example.projemanage.model.Board
import com.example.projemanage.model.Task
import com.example.projemanage.utils.Constants
import kotlinx.android.synthetic.main.activity_task_list.*

class TaskListActivity : BaseActivity() {

    private lateinit var mBoardDetails: Board

    private lateinit var boardDocumentId: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_list)

        if(intent.hasExtra(Constants.DOCUMENT_ID))
        {
            boardDocumentId= intent.getStringExtra(Constants.DOCUMENT_ID).toString()
        }
        showProgressDialog(resources.getString(R.string.please_wait))

        FireStoreClass().getBoardDetails(this@TaskListActivity, boardDocumentId)
    }

    private fun setupActionBar() {
        setSupportActionBar(toolbar_task_list_activity)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = mBoardDetails.name
        }
        toolbar_task_list_activity.setNavigationOnClickListener { onBackPressed() }
    }

//    A function to get the result of Board Detail.
    fun boardDetails(board: Board) {
        mBoardDetails= board
        hideProgressDialog()
        setupActionBar()

        // Here we are appending an item view for adding a list task list for the board.
        val addTaskList = Task(resources.getString(R.string.add_list))
        mBoardDetails.taskList.add(addTaskList)

        rv_task_list.layoutManager =
        LinearLayoutManager(this@TaskListActivity, LinearLayoutManager.HORIZONTAL, false)
        rv_task_list.setHasFixedSize(true)

        // Create an instance of TaskListItemsAdapter and pass the task list to it.
        val adapter = TaskListItemsAdapter(this@TaskListActivity, mBoardDetails.taskList)
        rv_task_list.adapter = adapter // Attach the adapter to the recyclerView.
    }

    // Create a function to get the result of add or updating the task list.
     fun addUpdateTaskListSuccess() {
        hideProgressDialog()

        showProgressDialog(resources.getString(R.string.please_wait))// Show the progress dialog.
         FireStoreClass().getBoardDetails(this@TaskListActivity, mBoardDetails.documentId)// Here get the updated board details.
    }

    // A function to get the task list name from the adapter class which we will be using to create a new task list in the database.
    fun createTaskList(taskListName: String) {
        Log.e("Task List Name", taskListName)

        val task = Task(taskListName, FireStoreClass().getCurrentUserID())
        mBoardDetails.taskList.add(0, task) // Add task to the first position of ArrayList
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1) // Remove the last position as we have added the item manually for adding the TaskList.
        // Show the progress dialog.
        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().addUpdateTaskList(this@TaskListActivity, mBoardDetails)
    }

    //  A function to update the taskList
    fun updateTaskList(position: Int, listName: String, model: Task){
        val task= Task(listName, model.createdBy)
        mBoardDetails.taskList[position]= task
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size- 1)
        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().addUpdateTaskList(this@TaskListActivity, mBoardDetails)
    }

    // A function to delete the task list from database.
    fun deleteTaskList(position: Int){
        mBoardDetails.taskList.removeAt(position)
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size- 1)
        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().addUpdateTaskList(this@TaskListActivity, mBoardDetails)
    }
}