package com.example.gripmoney.Login

import android.content.Intent
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.core.content.edit
import com.example.gripmoney.MainActivity
import com.example.gripmoney.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

//theb dhggdwgdwgdwgcyh ndbghewcgewcgewgbhewyghyu

//Hello world


class LoginChoice : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private companion object{
        private const val RC_SIGN_IN = 100
        private const val TAG = "GOOGLE_SIGN_IN_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_choice)
        //configure the Google SignIn
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()// we only need email from google account
            .build()
        val sharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)
        auth = FirebaseAuth.getInstance()

        val btnSignInEmail = findViewById<Button>(R.id.btnSignInEmail)
        val btnSignUp = findViewById<Button>(R.id.btnSignUp)
        val btnSignInGoogle = findViewById<Button>(R.id.btnSignInGoogle)
        btnSignInGoogle.setOnClickListener{
            Log.d(TAG,"onCreate: begin Google SignIn")
            val intent = googleSignInClient.signInIntent
            startActivityForResult(intent, RC_SIGN_IN)
        }
        btnSignInEmail.setOnClickListener{
            startActivity(Intent(this, LoginEmail::class.java))
            finish()
        }
        btnSignUp.setOnClickListener{
            startActivity(Intent(this, RegisterEmail::class.java))
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode:Int, data: Intent?){
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode== RC_SIGN_IN) {
            Log.d(TAG, "OnActivityResult: Google Sign In intent result")
            val accountTask = GoogleSignIn.getSignedInAccountFromIntent(data)
            //google sign in success, now auth with firebase
            val account = accountTask.getResult(ApiException::class.java)
            val sharedPref = this.getSharedPreferences("UtmNews-data", Context.MODE_PRIVATE)
            sharedPref.edit{
                putString("GoogleIDToken", account!!.idToken.toString())
            }
            firebaseAuthWithGoogleAccount(account)
        }}

    override fun onStart() {
        super.onStart()
        val currUser: FirebaseUser? = auth.currentUser
        if (currUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
    private fun firebaseAuthWithGoogleAccount(account: GoogleSignInAccount?){
        Log.d(TAG, "firebaseAuthWithGoogleAccount: begin firebase auth with google account")
        val credential = GoogleAuthProvider.getCredential(
            account!!.idToken,null
        )
        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                    authResult ->
                Log.d(TAG ,"firebaseAuthWithGoogleAccount: LoggedIn")
                val firebaseUser = auth.currentUser
                val uid = firebaseUser!!.uid
                val email = firebaseUser.providerData?.get(1)?.email.toString()
                Log.d(TAG,"firebaseAuthWithGoogleAccount: Uid: $uid")
                Log.d(TAG, "firebaseAuthWithGoogleAccount: Email: $email")
                //check if user is new or existing
                if(authResult.additionalUserInfo!!.isNewUser){
                    Log.d(TAG,"firebaseAuthWithGoogleAccount: Account created... \n$email")
                    Toast.makeText(this,"Account created... \n$email", Toast.LENGTH_SHORT).show()
                    val uName = hashMapOf("name" to "-")
                    var db = FirebaseFirestore.getInstance()
                    db.collection(uid).document("username").set(uName)
                        .addOnSuccessListener { documentReference ->
                            Toast.makeText(this, "Your username was successfully saved!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Your username was failed to saved!", Toast.LENGTH_SHORT).show()
                        }
                    val pin = hashMapOf("number" to "0")
                    db.collection(uid).document("pin").set(pin)
                        .addOnSuccessListener { documentReference ->
                            Toast.makeText(this, "Your pin was successfully saved!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Your pin was failed to saved!", Toast.LENGTH_SHORT).show()
                        }
                }
                else{
                    Log.d(TAG,"firebaseAuthWithGoogleAccount: Existing user... \n$email")
                    Toast.makeText(this,"LoggedIn... \n$email", Toast.LENGTH_SHORT).show()
                }
                Toast.makeText(this,"signInWithGoogle:success",Toast.LENGTH_LONG).show()
                val sharedPreference =  getSharedPreferences("PREFERENCE_NAME",Context.MODE_PRIVATE)
                val editor = sharedPreference.edit()
                //to get the user uid and pass it to main activity for linking with firebase firestore storage
                editor.putString("user",uid)
                editor.apply()
                startActivity(Intent(this, MainActivity::class.java))
            }
            .addOnFailureListener {
                    e -> Log.d(TAG,"firebaseAuthWithGoogleAccount: Log In Failed due to ${e.message}")
                Toast.makeText(this,"Log In Failed due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}