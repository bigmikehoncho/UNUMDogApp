package com.michael_mathieu.unumdogapp

import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dog_breed_holder.view.*
import org.json.JSONObject


class MainActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {

    private lateinit var adapter: DogBreedAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dogBreed_refresh.setOnRefreshListener(this)
        dogBreed_list.layoutManager = LinearLayoutManager(this)
        adapter = DogBreedAdapter()
        dogBreed_list.adapter = adapter

        if (savedInstanceState == null) {
            onRefresh()
        }

    }

    override fun onRefresh() {
        dogBreed_refresh.isRefreshing = true
        callAPI()
    }

    private fun callAPI() {
        val queue = Volley.newRequestQueue(this)
        val url = "https://dog.ceo/api/breeds/list/all"

        val stringRequest = StringRequest(Request.Method.GET, url,
                Response.Listener<String> { response ->
                    dogBreed_refresh.isRefreshing = false
                    dogBreed_error_text.visibility = View.GONE

                    val jsonObject = JSONObject(response)

                    val messages = jsonObject.get("message") as JSONObject

                    val dogBreeds = messages.keys().asSequence().toList()

                    adapter.setBreeds(dogBreeds)
                },
                Response.ErrorListener {
                    dogBreed_refresh.isRefreshing = false
                    dogBreed_error_text.visibility = View.VISIBLE
                    it.printStackTrace()
                })

        queue.add(stringRequest)
    }

    class DogBreedAdapter : RecyclerView.Adapter<DogBreedHolder>() {

        private val breeds = ArrayList<String>()
        private var lastPosition = -1

        init {

        }

        fun setBreeds(newBreeds: List<String>) {
            breeds.clear()
            breeds.addAll(newBreeds)
            lastPosition = -1
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DogBreedHolder {
            return DogBreedHolder(LayoutInflater.from(parent.context).inflate(R.layout.dog_breed_holder, parent, false))
        }

        override fun getItemCount() = breeds.size

        override fun onBindViewHolder(holder: DogBreedHolder, position: Int) {
            var breed = breeds[position]
            breed = breed.substring(0, 1).toUpperCase() + breed.substring(1)
            holder.name.text = breed
            setAnimation(holder.itemView, position)
        }


        private fun setAnimation(viewToAnimate: View, position: Int) {
            if (position > lastPosition) {
                val animation = AnimationUtils.loadAnimation(viewToAnimate.context, R.anim.item_fall_down)
                viewToAnimate.startAnimation(animation)
                lastPosition = position
            }
        }

        override fun onViewDetachedFromWindow(holder: DogBreedHolder) {
            holder.clearAnimation()
        }
    }

    class DogBreedHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name = view.dogBreed_name!!

        fun clearAnimation() {
            itemView.clearAnimation()
        }
    }
}
