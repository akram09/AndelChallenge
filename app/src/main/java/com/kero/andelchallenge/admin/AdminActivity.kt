package com.kero.andelchallenge.admin

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.firebase.storage.FirebaseStorage
import com.kero.andelchallenge.R
import com.kero.andelchallenge.databinding.AdminBinding
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase
import java.lang.NumberFormatException
import java.util.*


const val PICK_IMAGE = 5489
data class Product(val name:String="" , val price:Int=0 , val description:String="" , val imageUrl:String="")
class AdminActivity : AppCompatActivity() {
    var uri :Uri? = null
    lateinit var binding:AdminBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
         binding = DataBindingUtil.setContentView<AdminBinding>(this , R.layout.admin)
        setSupportActionBar(binding.toolbar)
        binding.button.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            val mimeTypes = arrayOf("image/jpeg", "image/png")
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode== PICK_IMAGE && resultCode == Activity.RESULT_OK){
            uri =data?.data
            if(uri!=null){
                binding.imageView.setImageURI(uri)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu , menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        item?.apply {
            if(itemId==R.id.save){
                saveItem()
            }
        }
        return true
    }
    private fun saveItem(){
        if(!binding.name.text.isNullOrBlank() && !binding.description.text.isNullOrBlank() && !binding.price.text.isNullOrBlank() && uri!=null) {
            var entered =false
            try {
                binding.price.text.toString().toInt()
            }catch (e:NumberFormatException){
                entered =true
            }finally {
                if(!entered ){
                    binding.progressBar.visibility = View.VISIBLE
                    FirebaseStorage.getInstance().reference.child("images").child(UUID.randomUUID().toString())
                        .putFile(uri!!).addOnCompleteListener{
                            if (it.isSuccessful){
                                val task = it.result?.storage?.downloadUrl
                               task?.addOnCompleteListener{
                                   if(it.isSuccessful){
                                             val url = it.result.toString()
                                             Toast.makeText(this@AdminActivity, url, Toast.LENGTH_LONG).show()
                                             FirebaseDatabase.getInstance().reference.child("product").push()
                                                 .setValue(Product(binding.name.text.toString()
                                                     , binding.price.text.toString().toInt() , binding.description.text.toString()
                                                     , url!!)).addOnCompleteListener {
                                                     binding.progressBar.visibility = View.GONE
                                                     if(it.isSuccessful){
                                                         binding.progressBar.visibility = View.GONE
                                                         Toast.makeText(this@AdminActivity , "Operation Succed", Toast.LENGTH_LONG).show()
                                                     }else{
                                                         binding.progressBar.visibility = View.GONE
                                                         Toast.makeText(this@AdminActivity , "an error ahs occured please verify your internet connection" , Toast.LENGTH_LONG).show()
                                                     }
                                                 }
                                         }else{
                                             binding.progressBar.visibility = View.GONE
                                             Toast.makeText(this@AdminActivity ,"connection problem", Toast.LENGTH_LONG).show()
                                            Log.e("errr", it.exception.toString())
                                         }

                                   }
                               }

                            }
                        }else{
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this , "an error ahs occured please verify your internet connection " , Toast.LENGTH_LONG).show()
                }
                }

        }else{
            binding.progressBar.visibility = View.GONE
            Toast.makeText(this , "please fill data ", Toast.LENGTH_LONG).show()
        }
    }
}