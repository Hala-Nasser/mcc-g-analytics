package com.example.googleanalyticsass

import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.googleanalyticsassignment.Adapter.CategoriesAdapter
import com.example.googleanalyticsassignment.modle.Category
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity() , CategoriesAdapter.onCategoryItemClickListener{

    lateinit var db: FirebaseFirestore
    private var progressDialog: ProgressDialog?=null
    private var mFirebaseAnalytics: FirebaseAnalytics? = null

    var start: Long = 0
    var end: Long = 0
    var total: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        start = Calendar.getInstance().timeInMillis

        db = Firebase.firestore
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        trackScreen("category screen")

        showDialog()
        getAllCategories()

        add_category.setOnClickListener {
            end = Calendar.getInstance().timeInMillis
            total = end - start

            val minutes: Long = total / 1000 / 60
            val seconds = (total / 1000 % 60)
            timeSpendInScreen("$minutes m $seconds s","hala123456","MainActivity")

            var i = Intent(this, AddCategory::class.java)
            startActivity(i)
        }
    }

    override fun onItemClick(data: Category, position: Int) {
        selectContent(data.id,data.nameCategory!!,data.imageCategory!!)
        end = Calendar.getInstance().timeInMillis
        total = end - start

        val minutes: Long = total / 1000 / 60
        val seconds = (total / 1000 % 60)
        timeSpendInScreen("$minutes m $seconds s","hala123456","MainActivity")


        var i = Intent(this,ProductsActivity::class.java)
        i.putExtra("id",data.id)
        i.putExtra("catImage",data.imageCategory)
        i.putExtra("catName",data.nameCategory)
        startActivity(i)

    }

    private fun getAllCategories(){
        val categoryList= mutableListOf<Category>()
        db.collection("categories")
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        for (document in task.result!!) {
                            Log.e("hala", "${document.id} -> ${document.get("category_name")} -> ${document.get("category_image")}")
                            val id = document.id
                            val data = document.data
                            val categoryName = data["category_name"] as String?
                            val categoryImage = data["category_image"] as String?
                            categoryList.add(Category(id, categoryImage, categoryName))
                        }
                        rv_category?.layoutManager =
                                LinearLayoutManager(this, RecyclerView.VERTICAL, false)
                        rv_category.setHasFixedSize(true)
                        val categoriesAdapter = CategoriesAdapter(this, categoryList, this)
                        rv_category.adapter = categoriesAdapter
                    }
                    hideDialog()
                }
    }

    private fun showDialog() {
        Log.e("hala","show dialog")
        progressDialog = ProgressDialog(this)
        progressDialog!!.setMessage("Loading Categories ...")
        progressDialog!!.setCancelable(false)
        progressDialog!!.show()
    }

    private fun hideDialog(){
        if(progressDialog!!.isShowing){
            Log.e("hala","hide dialog")
            progressDialog!!.dismiss()
        }
    }

    private fun trackScreen(screenName:String){
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
        bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, "MainActivity")
        mFirebaseAnalytics!!.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    private fun selectContent(id:String, name:String, contentType:String){
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id)
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name)
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, contentType)
        mFirebaseAnalytics!!.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
    }

    fun timeSpendInScreen(time: String, userId:String, pageName:String){

        val time= hashMapOf("time" to time,"userId" to userId,"pageName" to pageName)
        db.collection("Time")
                .add(time)
                .addOnSuccessListener {documentReference ->
                    Log.e("hala","time added successfully")
                }
                .addOnFailureListener {exception ->
                    Log.e("hala", exception.message.toString())
                }
    }




}