package com.qbra.filmquoteschat

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.qbra.filmquoteschat.databinding.RecyclerRowBinding

class FilmAdapter(val filmList: ArrayList<Film>) : RecyclerView.Adapter<FilmAdapter.FilmHolder>() {
    class FilmHolder(val binding: RecyclerRowBinding) : RecyclerView.ViewHolder(binding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilmHolder {
        val binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FilmHolder(binding)
    }

    override fun getItemCount(): Int {
        return filmList.size
    }

    override fun onBindViewHolder(holder: FilmHolder, position: Int) {
        holder.binding.textView.text = filmList[position].filmName
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, AddQuotesActivity::class.java)
            intent.putExtra("info", "old")
            intent.putExtra("id", filmList[position].id)
            holder.itemView.context.startActivity(intent)
        }
    }
}