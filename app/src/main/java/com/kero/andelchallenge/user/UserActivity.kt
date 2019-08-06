package com.kero.andelchallenge.user

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kero.andelchallenge.R
import com.kero.andelchallenge.admin.Product
import kotlinx.android.synthetic.main.user.*

class UserActivity :AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(toolbar)
        setContentView(R.layout.user)
        FirebaseDatabase.getInstance().reference.child("product").addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                Toast.makeText(this@UserActivity , "an error occured ", Toast.LENGTH_LONG).show()
            }

            override fun onDataChange(p0: DataSnapshot) {
                val list= ArrayList<Product>()
                for (p in p0.children){
                    list.add(p.getValue(Product::class.java)!!)
                }
                findViewById<RecyclerView>(R.id.recycler_view).layoutManager = LinearLayoutManager(this@UserActivity , RecyclerView.VERTICAL , false)
                findViewById<RecyclerView>(R.id.recycler_view).adapter= Adapter(this@UserActivity , list)
            }
        })
    }
}