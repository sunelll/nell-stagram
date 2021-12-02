package com.nell.nellstargram

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.TwitterAuthProvider
import com.twitter.sdk.android.core.*
import com.twitter.sdk.android.core.identity.TwitterAuthClient
import kotlinx.android.synthetic.main.activity_login.*
import android.widget.Toast.makeText as makeText1


class LoginActivity : AppCompatActivity() {
    //firebase 관리 클래스
    var auth : FirebaseAuth? = null
    var googleSignInClient : GoogleSignInClient? = null
    var GOOGLE_LOGIN_CODE = 9001

    lateinit var twitterAuthClient: TwitterAuthClient
    var loginState = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()

        email_login_btn.setOnClickListener {
            signinAndSignup()
        }
        google_login_btn.setOnClickListener {
            //first step
            googleLogin()
        }
        twitter_login_btn.setOnClickListener {
            twitterLogin()
        }

        //google login
        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Configure Twitter SDK
//        val authConfig = TwitterAuthConfig(
//                getString(R.string.twitter_consumer_key),
//                getString(R.string.twitter_consumer_secret)
//        )

        val authConfig = TwitterAuthConfig(
                BuildConfig.TWITTER_API_KEY,
                BuildConfig.TWITTER_API_SECRET_KEY
        )

        val twitterConfig = TwitterConfig.Builder(this)
                .twitterAuthConfig(authConfig)
                .build()

        Twitter.initialize(twitterConfig)
        twitterAuthClient = TwitterAuthClient()

    }

    fun signOut(){

    }

    //자동 로그인
    override fun onStart() {
        super.onStart()
        moveMainPage(auth?.currentUser)
    }

    fun twitterLogin(){
        twitterAuthClient?.authorize(this, object : Callback<TwitterSession>() {
            override fun success(result: com.twitter.sdk.android.core.Result<TwitterSession>?) {
                if (result != null) {
                    handleTwitterSession(result.data)
                    Log.d(TAG, "signInWithCredential:success")
                    loginState = !loginState
                }
            }
            override fun failure(exception: TwitterException?) {
                //fail, error
                Log.d(TAG, "twitterLogin:failure_exception")
            }

        })


    }

    fun handleTwitterSession(session: TwitterSession) {
        val credential = TwitterAuthProvider.getCredential(
                session.authToken.token,
                session.authToken.secret
        )
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener(this) { task ->
                //var email = auth!!.currentUser?.email
                //var uid = auth!!.currentUser?.uid
                //var user = arrayOf(email, uid)

                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("MainActivity", "signInWithCredential:success")
                    moveMainPage(task.result?.user)

                } else {
                    //fail
                    // If sign in fails, display a message to the user.
                    Log.w("MainActivity", "signInWithCredential:failure", task.exception)
                }
            }
    }




    fun googleLogin(){
        var signInIntent = googleSignInClient?.signInIntent
        startActivityForResult(signInIntent, GOOGLE_LOGIN_CODE)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == GOOGLE_LOGIN_CODE){
            var result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if(result.isSuccess){
                try {
                    var accont = result.signInAccount
                    //Second Step
                    firebaseAuthWithGoogle(accont)
                } catch (e: Exception) {
                    makeText1(this, "Login error", Toast.LENGTH_SHORT).show()
                }
            }
        }
        twitterAuthClient?.onActivityResult(requestCode, resultCode, data)
    }
    fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        var credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener { task ->
                if(task.isSuccessful){
                    //Login
                    moveMainPage(task.result?.user)
                }else{
                    //Show the error message
                    makeText1(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }
    }


    //회원가입 함수
    fun signinAndSignup(){
        auth?.createUserWithEmailAndPassword(email_edittext.text.toString(), password_edittext.text.toString())
            ?.addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        //Createing a user accout
                        makeText1(this, "가입 되었습니다", Toast.LENGTH_SHORT).show()
                        moveMainPage(task.result?.user)
                    }else if(task.exception?.message.isNullOrEmpty()){
                        //Show the error message
                        makeText1(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                    }else{
                        //Login if you have accout
                        signinEmail()
                    }
                }
        }

    fun signinEmail(){
        auth?.signInWithEmailAndPassword(email_edittext.text.toString(), password_edittext.text.toString())
            ?.addOnCompleteListener { task ->
                if(task.isSuccessful){
                    //Login
                    moveMainPage(task.result?.user)
                }else if(task.isCanceled){
                    makeText1(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                }else{
                    //Show the error message
                    makeText1(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

    fun moveMainPage(user: FirebaseUser?){
        if(user != null){
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            }
        }



}