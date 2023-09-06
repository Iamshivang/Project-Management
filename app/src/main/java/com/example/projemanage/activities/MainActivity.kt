package com.example.projemanage.activities
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.projemanage.MyProfileActivity
import com.example.projemanage.R
import com.example.projemanage.adapters.BoardItemsAdapters
import com.example.projemanage.firebase.FireStoreClass
import com.example.projemanage.model.Board
import com.example.projemanage.model.User
import com.example.projemanage.utils.Constants
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.main_content.*
import kotlinx.android.synthetic.main.nav_header_main.*

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    companion object { // A companion object to declare the constants.
        //A unique code for starting the activity for result
        const val MY_PROFILE_REQUEST_CODE: Int = 11

        const val CREATE_BOARD_REQUEST_CODE: Int= 12
    }

    private lateinit var mUserName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setUpToggle()
        nav_view.setNavigationItemSelectedListener(this)

        FireStoreClass().loadUserData(this, true)

        fab_create_board.setOnClickListener {
            val intent= Intent(this@MainActivity, CreateBoardActivity::class.java)
            intent.putExtra(Constants.NAME, mUserName)
            startActivityForResult(intent, CREATE_BOARD_REQUEST_CODE)
        }

    }

    override fun onBackPressed() {

        if(drawerLayout.isDrawerOpen(GravityCompat.START))
        {
            drawerLayout.closeDrawer(GravityCompat.START)
        }else{
            doubleBackToExit()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        item.isChecked= true

        when(item.itemId){

            R.id.nav_my_profile ->{
                startActivityForResult(Intent(applicationContext, MyProfileActivity::class.java), MY_PROFILE_REQUEST_CODE)
            }

            R.id.nav_sign_out ->{
                FirebaseAuth.getInstance().signOut()
                val intent= Intent(applicationContext, IntroActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }

        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    // Add the onActivityResult function and check the result of the activity for which we expect the result.
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode== Activity.RESULT_OK && requestCode== MY_PROFILE_REQUEST_CODE){
            FireStoreClass().loadUserData(this@MainActivity)  // Here not need to get boardArrayList from firebase, so isToReadBoardsList: Boolean is introduced
        }else if (resultCode== Activity.RESULT_OK && requestCode== CREATE_BOARD_REQUEST_CODE){
            FireStoreClass().getBoardList(this)
        } else{
            Log.e("Cancelled", "Cancelled")
        }
    }

    private fun setUpToggle(){
        setSupportActionBar(toolbar_main_activity)

        val toggle:ActionBarDrawerToggle= ActionBarDrawerToggle(this, drawerLayout, toolbar_main_activity, R.string.OpenDrawer, R.string.CloseDrawer)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
    }

    fun updateNavigationUserDetails(user: User, isToReadBoardsList: Boolean){

        mUserName= user.name

        Glide
            .with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(nav_user_image)

        tv_username.text= user.name

        // Here if the isToReadBoardList is TRUE then get the list of boards
        if (isToReadBoardsList) {
            // Show the progress dialog.
            showProgressDialog(resources.getString(R.string.please_wait))
            FireStoreClass().getBoardList(this@MainActivity)
        }

    }

    fun populateBoardsListToUI(boardArrayList: ArrayList<Board>){
        hideProgressDialog()

        if(boardArrayList.size> 0){
            rv_boards_list.visibility = View.VISIBLE
            no_boards_available.visibility = View.GONE

            rv_boards_list.layoutManager= LinearLayoutManager(this)
            rv_boards_list.setHasFixedSize(true)

            // Create an instance of BoardItemsAdapter and pass the boardList to it.
            val adapter= BoardItemsAdapters(this, boardArrayList)
            rv_boards_list.adapter= adapter

            //  Add click event for boards item and launch the TaskListActivity

            adapter.setOnClickListener(object: BoardItemsAdapters.OnClickListener{
                override fun onClick(position: Int, model: Board) {
                    val intent= Intent(this@MainActivity, TaskListActivity::class.java)
                    intent.putExtra(Constants.DOCUMENT_ID, model.documentId)
                    startActivity(intent)
                }

            })
        }else{
            rv_boards_list.visibility = View.GONE
            no_boards_available.visibility = View.VISIBLE
        }
    }
}