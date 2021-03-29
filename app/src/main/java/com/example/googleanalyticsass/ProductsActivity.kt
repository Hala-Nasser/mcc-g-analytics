package com.example.googleanalyticsass

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.GridLayoutManager
import com.example.googleanalyticsass.Adapter.ProductAdminAdapter
import com.example.googleanalyticsass.modle.Products
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_products.*
import java.util.*

class ProductsActivity : AppCompatActivity(), ProductAdminAdapter.onProductsItemClickListener {

    lateinit var db: FirebaseFirestore
    var catImage:String?=null
    private var progressDialog: ProgressDialog?=null
    private var mFirebaseAnalytics: FirebaseAnalytics? = null

    var start: Long = 0
    var end: Long = 0
    var total: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_products)

        start = Calendar.getInstance().timeInMillis

        db = Firebase.firestore
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        trackScreen("product screen")

        showDialog()
            catImage=intent.getStringExtra("catImage")
            val catName=intent.getStringExtra("catName")
            Picasso.get().load(catImage).into(category_image)
            txt_category_name.text=catName
            getProductsAccordingToCategory("$catName")

        add_product.setOnClickListener {
            end = Calendar.getInstance().timeInMillis
            total = end - start

            val minutes: Long = total / 1000 / 60
            val seconds = (total / 1000 % 60)
            timeSpendInScreen("$minutes m $seconds s","hala123456","ProductActivity")

            var i = Intent(this, AddProduct::class.java)
            startActivity(i)
        }

        category_back.setOnClickListener {
            end = Calendar.getInstance().timeInMillis
            total = end - start

            val minutes: Long = total / 1000 / 60
            val seconds = (total / 1000 % 60)
            timeSpendInScreen("$minutes m $seconds s","hala123456","ProductActivity")

            var i = Intent(this, MainActivity::class.java)
            startActivity(i)
        }

    }

    override fun onItemClick(data: Products, position: Int) {
        selectContent(data.id!!,data.name!!,data.image!!)

        end = Calendar.getInstance().timeInMillis
        total = end - start

        val minutes: Long = total / 1000 / 60
        val seconds = (total / 1000 % 60)
        timeSpendInScreen("$minutes m $seconds s","hala123456","ProductActivity")

        var i = Intent(this,ProductDetails::class.java)
        i.putExtra("pid",data.id)
        i.putExtra("pname",data.name)
        i.putExtra("pimage",data.image)
        i.putExtra("pprice",data.price)
        i.putExtra("pdescription",data.description)
        i.putExtra("pcategory",data.categoryName)
        startActivity(i)

    }

    private fun getProductsAccordingToCategory(catName:String){
        val dataProduct = mutableListOf<Products>()

        db.collection("products").whereEqualTo("categoryName",catName)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        Log.e("hala_product", "${document.id} -> ${document.get("name")}")
                        val id = document.id
                        val data = document.data
                        val name = data["name"] as String?
                        val image = data["image"] as String?
                        val price = data["price"] as Double
                        val description = data["description"] as String?
                        val categoryName = data["categoryName"] as String?
                        dataProduct.add(
                            Products(id,image,name,price,description,categoryName)
                        )
                    }

                    rv_product.layoutManager = GridLayoutManager(this, 2)
                    rv_product.setHasFixedSize(true)
                    val productAdapter = ProductAdminAdapter(this, dataProduct,this)
                    rv_product.adapter = productAdapter

                }
                hideDialog()
            }
    }

    private fun showDialog() {
        Log.e("hala","show dialog")
        progressDialog = ProgressDialog(this)
        progressDialog!!.setMessage("Loading Products ...")
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