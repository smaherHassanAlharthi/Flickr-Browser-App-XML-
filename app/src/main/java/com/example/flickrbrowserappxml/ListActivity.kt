package com.example.flickrbrowserappxml


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.*
import kotlin.collections.ArrayList
import androidx.recyclerview.widget.LinearSnapHelper

import androidx.recyclerview.widget.SnapHelper
import kotlinx.coroutines.*
import java.net.HttpURLConnection
import java.net.URL


class ListActivity : AppCompatActivity() {



    private lateinit var myRv: RecyclerView
    private lateinit var rvAdapter: RVAdapter
    var imagesNumber=24
    var feeds = ArrayList<Photo>()
    var filteredPhotos=feeds
    val parser = XMLParser()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pager)

        //get the keyword
        val keyword= intent.getStringExtra("key") //get the keyword

        setBottomNivigation()

        if (keyword!!.isNotEmpty()) {
            //call the api interface to retrieve the data
            getFeeds("https://www.flickr.com/services/rest/?method=${Constants.METHOD_SEARCH}&api_key=${Constants.API_KEY}" +
                    "&tags=${keyword}&per_page=${imagesNumber}&safe_search=1&format=rest")
        }
        else
        //call the api interface to retrieve the data
            getFeeds("https://www.flickr.com/services/rest/?method=${Constants.METHOD_SEARCH}&api_key=${Constants.API_KEY}" +
                    "&tags=Cats&per_page=${imagesNumber}&safe_search=1&format=rest")


    }

    fun fetch(url:String): ArrayList<Photo> {
        val url =
            URL(url)
        val urlConnection = url.openConnection() as HttpURLConnection
        feeds =

            urlConnection.getInputStream()?.let {
                parser.parse(it)
            }
                    as ArrayList<Photo>
        return feeds
    }

    private fun getFeeds(url:String) {
        CoroutineScope(Dispatchers.IO).launch {

            feeds = async {
                fetch(url)
            }.await()

            withContext(Dispatchers.Main) {
                setRV()
            }

        }
    }

    private fun setBottomNivigation() {
        val bottomNavigationView=findViewById<BottomNavigationView>(R.id.bottomNavigation)
        val menu: Menu = bottomNavigationView.menu
        val menuItem: MenuItem = menu.getItem(1)
        menuItem.isChecked = true

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.mainActivity-> startActivity(Intent(this,MainActivity::class.java))
                R.id.listView-> startActivity(Intent(this,ListActivity::class.java))
                R.id.likedActivity-> startActivity(Intent(this,SavedActivity::class.java))
            }
            true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_menu, menu)
        val menuItem = menu?.findItem(R.id.action_search)
        if(feeds!=null){
            if (menuItem != null) {
                val searchItem = menuItem.actionView as SearchView
                searchItem.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        return true
                    }
                    override fun onQueryTextChange(newText: String?): Boolean {
                        if (newText!!.isNotEmpty()) {
                            filteredPhotos.clear()
                            val search = newText!!.toLowerCase(Locale.getDefault())
                            feeds.forEach {
                                if (it.title?.toLowerCase(Locale.getDefault()).toString()
                                        .contains(search)
                                ) {
                                    filteredPhotos.add(it)
                                }
                            }
                            rvAdapter.updateList(filteredPhotos)
                            myRv.adapter!!.notifyDataSetChanged()
                        } else {
                            filteredPhotos.clear()
                            filteredPhotos.addAll(feeds)
                            rvAdapter.updateList(filteredPhotos)
                            myRv.adapter!!.notifyDataSetChanged()
                        }
                        return true
                    }
                })}
        }
        return true
    }

    fun setRV() {
        myRv = findViewById(R.id.rvPhotosPager)
        val snapHelper: SnapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(myRv)
        rvAdapter =RVAdapter(feeds, this)
        myRv.adapter = rvAdapter
        myRv.setLayoutManager(
            LinearLayoutManager(
                this,
                LinearLayoutManager.HORIZONTAL, false
            )
        )

    }



}