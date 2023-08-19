package com.qbra.filmquoteschat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.qbra.filmquoteschat.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var filmList: ArrayList<Film>
    private lateinit var filmAdapter: FilmAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        filmList = ArrayList<Film>()

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        filmAdapter = FilmAdapter(filmList)
        binding.recyclerView.adapter = filmAdapter

        try
        {
            val database = this.openOrCreateDatabase("Quotes", MODE_PRIVATE, null)
            val cursor = database.rawQuery("SELECT * FROM quotes", null)
            val filmNameIx = cursor.getColumnIndex("filmName")
            val idIx = cursor.getColumnIndex("id")

            while (cursor.moveToNext())
            {
                val name = cursor.getString(filmNameIx)
                val id = cursor.getInt(idIx)
                val film = Film(name, id)
                filmList.add(film)
            }
            filmAdapter.notifyDataSetChanged()
            cursor.close()
        }
        catch (e : Exception)
        {
            e.printStackTrace()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.quote_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if(item.itemId == R.id.add_quote)
        {
            val intent = Intent(this@MainActivity, AddQuotesActivity::class.java)
            intent.putExtra("info", "new")
            startActivity(intent)
        }

        return super.onOptionsItemSelected(item)
    }
}