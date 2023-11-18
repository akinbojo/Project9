package com.example.project9

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.android.synthetic.main.fragment_images.view.*

class ImagesFragment : Fragment() {

    private val firestore = FirebaseFirestore.getInstance()
    private val imagesCollection = firestore.collection("images")

    private lateinit var imageAdapter: ImageAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_image, container, false)

        // Setting up RecyclerView
        imageAdapter = ImageAdapter(emptyList()) { imageUrl ->
            // Handle item click (e.g., open full-screen image)
        }

        view.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = imageAdapter
        }

        // Loading images from Firestorage
        loadImages()
        return view
    }

    private fun loadImages() {
        imagesCollection.get()
            .addOnSuccessListener { querySnapshot ->
                val images = mutableListOf<String>()
                for (document in querySnapshot.documents) {
                    val imageUrl = document.getString("imageUrl")
                    imageUrl?.let { images.add(it) }
                }
                imageAdapter.updateData(images)
            }
            .addOnFailureListener { exception ->
                //Handling errors

            }
    }
}
