package com.qbra.filmquoteschat

import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.qbra.filmquoteschat.databinding.ActivityAddQuotesBinding
import java.io.ByteArrayOutputStream

class AddQuotesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddQuotesBinding
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var database: SQLiteDatabase
    var selecetedBitmap : Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddQuotesBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        database = this.openOrCreateDatabase("Quotes", MODE_PRIVATE, null)

        registerLauncher()

        val intent = intent
        val info = intent.getStringExtra("info")
        if (info.equals("new"))
        {
            binding.nameText.setText("")
            binding.quotesText.setText("")
            binding.button.visibility = View.VISIBLE
            binding.imageView.setImageResource(R.drawable.selectimage)
        }
        else
        {
            binding.button.visibility = View.INVISIBLE
            val selectedQuoteId = intent.getIntExtra("id", 0)
            val cursor = database.rawQuery("SELECT * FROM quotes WHERE id = ?", arrayOf(selectedQuoteId.toString()))

            val filmNameIx = cursor.getColumnIndex("filmName")
            val quoteIx = cursor.getColumnIndex("filmQuote")
            val imageIx = cursor.getColumnIndex("image")

            while (cursor.moveToNext()){
                binding.nameText.setText(cursor.getString(filmNameIx))
                binding.quotesText.setText(cursor.getString(quoteIx))

                val byteArray = cursor.getBlob(imageIx)
                val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                binding.imageView.setImageBitmap(bitmap)
            }
            cursor.close()
        }
    }

    fun selectImage(view : View) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        {
            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED)
            {
                if(ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_MEDIA_IMAGES))
                {
                    Snackbar.make(view, "Permission needed for gallery", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", View.OnClickListener {
                        permissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
                    }).show()
                }
                else
                {
                    permissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
                }
            }
            else
            {
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
        }
        else
        {
            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            {
                if(ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_EXTERNAL_STORAGE))
                {
                    Snackbar.make(view, "Permission needed for gallery", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", View.OnClickListener {
                        permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    }).show()
                }
                else
                {
                    permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
            else
            {
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
        }

    }

    fun save(view : View) {
        val filmName = binding.nameText.text.toString()
        val quote = binding.quotesText.text.toString()

        if(selecetedBitmap != null)
        {
            val smallBitmap = smallerImage(selecetedBitmap!!, 300)

            //görselin veriye çevrilişi
            val outputStream = ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG, 50, outputStream)
            val byteArray = outputStream.toByteArray()

            try
            {
                database.execSQL("CREATE TABLE IF NOT EXISTS quotes (id INTEGER PRIMARY KEY, filmName VARCHAR, filmQuote VARCHAR, image BLOB)")

                val sqlString = "INSERT INTO quotes (filmName, filmQuote, image) VALUES (?, ?, ?)"
                val statement = database.compileStatement(sqlString)
                statement.bindString(1, filmName)
                statement.bindString(2, quote)
                statement.bindBlob(3, byteArray)
                statement.execute()

            }
            catch (e : Exception)
            {
                e.printStackTrace()
            }

            val intent = Intent(this@AddQuotesActivity, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }
    }

    private fun registerLauncher() {
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {result ->
            if(result.resultCode == RESULT_OK) {
                val intentFromResult = result.data
                if(intentFromResult != null) {
                    val imageData = intentFromResult.data
                    if(imageData != null){
                        try {
                            if(Build.VERSION.SDK_INT >= 28)
                            {
                                val source = ImageDecoder.createSource(this@AddQuotesActivity.contentResolver, imageData)
                                selecetedBitmap = ImageDecoder.decodeBitmap(source)
                                binding.imageView.setImageBitmap(selecetedBitmap)
                            }
                            else
                            {
                                selecetedBitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageData)
                                binding.imageView.setImageBitmap(selecetedBitmap)
                            }
                        }
                        catch (e : java.lang.Exception){
                            e.printStackTrace()
                        }
                    }
                }
            }
        }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {result ->
            if(result)
            {
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
            else
            {
                Toast.makeText(this@AddQuotesActivity, "Permission Needed", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun smallerImage(img : Bitmap, maxSize : Int) : Bitmap {

        var width = img.width
        var height = img.height
        var bitmapRatio : Double = width.toDouble() / height.toDouble()

        if(bitmapRatio > 1)
        {
            width = maxSize
            val scaledHeight = width / bitmapRatio
            height = scaledHeight.toInt()
        }
        else
        {
            height = maxSize
            val scaledWidth = height * bitmapRatio
            width = scaledWidth.toInt()
        }

        return Bitmap.createScaledBitmap(img, width, height, false)
    }
}