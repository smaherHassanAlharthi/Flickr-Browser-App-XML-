package com.example.flickrbrowserappxml

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flickrbrowserappxml.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.*
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {
    private lateinit var myRv: RecyclerView
    private lateinit var rvAdapter: RVAdapter
    lateinit var lastList: ArrayList<Photo>
    var keyword=""
    var imagesNumber=24
    lateinit var photos : ArrayList<Photo>
    val parser = XMLParser()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //hide action bar
        getSupportActionBar()?.hide()

        //initialize shared preference
        PreferenceHelper.init(this)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btSearch.setOnClickListener {
            hideKeyboard()
            if (binding.etSearch.text.isNotEmpty()) {

                //if the user put the number of images change to the new number
                if(binding.etNumber.text.isNotEmpty()){
                    imagesNumber=binding.etNumber.text.toString().toInt()
                    //change number hint
                    binding.etNumber.hint=imagesNumber.toString()
                }
                keyword=binding.etSearch.text.toString()
                //call the api interface to retrieve the data
                getFeeds("https://www.flickr.com/services/rest/?method=${Constants.METHOD_SEARCH}&api_key=${Constants.API_KEY}" +
                        "&tags=${keyword}&per_page=${imagesNumber}&safe_search=1&format=rest")

            }
            else
                Toast.makeText(this,"Type something", Toast.LENGTH_SHORT).show()
        }

        setBottomNivigation()
        getFeeds("https://www.flickr.com/services/rest/?method=${Constants.METHOD_SEARCH}&api_key=${Constants.API_KEY}" +
                "&tags=${keyword}&per_page=${imagesNumber}&safe_search=1&format=rest")

    }

    private fun getFeeds(url:String) {
        CoroutineScope(Dispatchers.IO).launch {

            photos = async {
                fetch(url)
            }.await()

            withContext(Dispatchers.Main) {
                lastList=photos
                setRV(photos)
            }

        }
    }

    fun setRV(photos: ArrayList<Photo>) {
        myRv = findViewById(R.id.rvPhotos)
        if(myRv.adapter==null)
        {
            rvAdapter =RVAdapter(photos, this)
            myRv.adapter = rvAdapter
            myRv.layoutManager = GridLayoutManager(applicationContext,2)

        }
        else
        {
            //this for DiffUtil
            rvAdapter.updateList(photos)
        }

    }

    fun fetch(url:String): ArrayList<Photo> {
        val url =
            URL(url)
        val urlConnection = url.openConnection() as HttpURLConnection
        photos =

            urlConnection.getInputStream()?.let {
                parser.parse(it)
            }
                    as ArrayList<Photo>
        return photos
    }


    private fun setBottomNivigation() {
        val bottomNavigationView=findViewById<BottomNavigationView>(R.id.bottomNavigation)
        val menu: Menu = bottomNavigationView.menu
        val menuItem: MenuItem = menu.getItem(0)
        menuItem.isChecked = true

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.mainActivity-> startActivity(Intent(this,MainActivity::class.java))
                R.id.listView-> { val intent = Intent(this, ListActivity::class.java)
                    intent.putExtra("key",keyword) //send the keyword
                    startActivity(intent)
                }
                R.id.likedActivity-> startActivity(Intent(this,SavedActivity::class.java))
            }
            true
        }

    }


    fun hideKeyboard()
    {
        // Hide Keyboard
        val hideKeyboard = ContextCompat.getSystemService(this, InputMethodManager::class.java)
        hideKeyboard?.hideSoftInputFromWindow(this.currentFocus?.windowToken, 0)
    }

    override fun onPause() {
        super.onPause()
        PreferenceHelper.setItemList(PreferenceHelper.LAST_PHOTOS_LIST, lastList)
    }

    override fun onResume() {
        super.onResume()
        //check if last search in shared preference if yes set rv
        lastList= PreferenceHelper.getItemList(PreferenceHelper.LAST_PHOTOS_LIST)

        //if not empty set RV
        if(lastList!=null) {
            setRV(lastList)
        }
    }



}

