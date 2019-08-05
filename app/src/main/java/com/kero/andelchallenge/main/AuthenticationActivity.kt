package com.kero.andelchallenge.main

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.FirebaseAuth
import com.kero.andelchallenge.R
import com.kero.andelchallenge.databinding.ActivityMainBinding
import com.kero.andelchallenge.mail_auth.SignUpActivity
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.kero.andelchallenge.admin.AdminActivity
import com.kero.andelchallenge.user.UserActivity
import com.kero.andelchallenge.utils.Fail
import com.kero.andelchallenge.utils.Loading
import com.kero.andelchallenge.utils.Success
import kotlinx.android.synthetic.main.activity_main.*


const val SIGN_IN = 57894
class AuthenticationActivity : AppCompatActivity() {

    private lateinit var binding :ActivityMainBinding
    private val viewModel = ViewModelProviders.of(this)[AuthViewModel::class.java]
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this , R.layout.activity_main)
        viewModel.observe(this ){
            var authState = it.authState
            when(authState){
                is Success<UserType> ->{
                    binding.progressBar.visibility = View.GONE
                    if(authState()!=UserType.NON_AUth) {
                        startActivity(
                            Intent(
                                this,
                                if (authState() == UserType.SIMPLE_USER) UserActivity::class.java else AdminActivity::class.java
                            )

                        )
                        finish()
                    }
                }
                is Fail<*, *> ->{
                    binding.progressBar.visibility =View.GONE
                    Toast.makeText(this , "An error occured please verify your internt connection",Toast.LENGTH_LONG).show()
                }
                is Loading<*> ->{
                    binding.progressBar.visibility = View.VISIBLE
                }

            }
        }
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        binding.introGoogleSignIn.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, SIGN_IN)
        }
        binding.introEmailSignIn.setOnClickListener {
            startActivity(Intent(this , SignUpActivity::class.java))
            finish()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
                viewModel.signInCredential(credential)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.e("errr", "google sign in failed")
                Toast.makeText(this , "Google sign in failed " , Toast.LENGTH_LONG).show()
            }
        }
    }
}
