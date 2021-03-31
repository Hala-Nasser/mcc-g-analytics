package com.example.googleanalyticsass

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_product_details.*
import java.util.*

class ProductDetails : AppCompatActivity() {
    lateinit var db: FirebaseFirestore
    private var mFirebaseAnalytics: FirebaseAnalytics? = null

    var start: Long = 0
    var end: Long = 0
    var total: Long = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_details)

        start = Calendar.getInstance().timeInMillis

        db= Firebase.firestore
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        trackScreen("product details screen")

            val pname = intent.getStringExtra("pname")
            val pimage = intent.getStringExtra("pimage")
            val pprice = intent.getDoubleExtra("pprice",0.0)
            val pdescription = intent.getStringExtra("pdescription")

            Picasso.get().load(pimage).into(product_image)
            product_name.text = pname
            product_price.text = pprice.toString()
            product_desc.text = pdescription


        back_to_products.setOnClickListener {
            end = Calendar.getInstance().timeInMillis
            total = end - start

            val minutes: Long = total / 1000 / 60
            val seconds = (total / 1000 % 60)
            timeSpendInScreen("$minutes m $seconds s","hala123456","ProductDetails")

            onBackPressed()
        }

}

    private fun trackScreen(screenName:String){
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
        bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, "ProductDetails")
        mFirebaseAnalytics!!.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
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