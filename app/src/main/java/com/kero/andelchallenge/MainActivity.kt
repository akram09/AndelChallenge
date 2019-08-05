package com.kero.andelchallenge

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth

const val SIGN_IN = 57894
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(FirebaseAuth.getInstance().currentUser !=null){

        }
        setContentView(R.layout.activity_main)
    }




}
