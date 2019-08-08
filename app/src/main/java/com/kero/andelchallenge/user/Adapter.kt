package com.kero.andelchallenge.user

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kero.andelchallenge.R
import com.kero.andelchallenge.admin.Product
import com.squareup.picasso.Picasso

class Adapter(val context: Context, val list :List<Product>):RecyclerView.Adapter<Adapter.ProductViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
       return ProductViewHolder(LayoutInflater.from(context).inflate(R.layout.product , parent , false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
         holder.updateUi(list[position])
    }

    inner class ProductViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        private val imageView = itemView.findViewById<ImageView>(R.id.imageView2)
        private val productName =itemView.findViewById<TextView>(R.id.textView4)
        private val description = itemView.findViewById<TextView>(R.id.textView6)
        private val price =itemView.findViewById<TextView>(R.id.textView7)
        fun updateUi(product : Product){
            Picasso.get().load(product.imageUrl).into(imageView)
            productName.text = product.name
            description.text = product.description
            price.text = product.price.toString()
        }

    }
}