package com.example.project9

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = Firebase.auth

        if (savedInstanceState == null) {
            // Loading the ImagesFragment by default
            loadFragment(ImagesFragment())
        }

        // Check if the user is already logged in, if yes, navigate to the next screen
        if (checkIfUserLoggedIn()) {
            navigateToNextScreen()
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    // Function to check if a user is already signed in
    private fun checkIfUserLoggedIn(): Boolean {
        val currentUser = auth.currentUser
        return currentUser != null
    }

    // Function to sign in
    private fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success
                    val user = auth.currentUser
                    navigateToNextScreen()
                } else {
                    // If sign in fails, display a message to the user.
                    showMessage("Authentication failed. Please try again.")
                }
            }
    }

    // Function to sign out
    private fun signOut() {
        auth.signOut()
        navigateToLoginScreen()
    }

    // Function to get the current user
    private fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    // Function to navigate to the next screen after successful sign-in
    private fun navigateToNextScreen() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Finish the current activity to prevent going back to the login screen
    }

    // Function to navigate to the login screen after sign-out
    private fun navigateToLoginScreen() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish() // Finish the current activity to prevent going back to the logged-in state
    }

    // Function to display a message to the user
    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()


    }
}