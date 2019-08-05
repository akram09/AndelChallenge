package com.kero.andelchallenge.mail_auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.kero.andelchallenge.R
import com.kero.andelchallenge.databinding.SignUpBinding
import com.kero.andelchallenge.user.UserActivity
import com.kero.andelchallenge.utils.Fail
import com.kero.andelchallenge.utils.Loading
import com.kero.andelchallenge.utils.Success

class SignUpActivity : AppCompatActivity() {
    private val viewModel = ViewModelProviders.of(this )[SignUpViewModel::class.java]
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding  = DataBindingUtil.setContentView<SignUpBinding>(this , R.layout.sign_up)
        binding.btnSave.setOnClickListener {
            if(!binding.emailEdittext.text.isNullOrBlank() && !binding.fnameEdittext.text.isNullOrBlank() && !binding.passEdittext.text.isNullOrBlank())
            viewModel.signIn(binding.emailEdittext.text.toString() , binding.fnameEdittext.text.toString() , binding.passEdittext.text.toString())
        }
        viewModel.observe(this){
            it.state.getContentIfNotHandled()?.apply {
                when(this){
                    is Success ->{
                        binding.progressBar.visibility = View.GONE
                        startActivity(Intent(this@SignUpActivity , UserActivity::class.java))
                        finish()
                    }
                     is Fail<*, *> ->{
                         binding.progressBar.visibility =  View.GONE
                         Toast.makeText(this@SignUpActivity , "an error occured", Toast.LENGTH_LONG).show()
                     }
                      is Loading ->  binding.progressBar.visibility = View.VISIBLE
                }
            }
        }
    }
}