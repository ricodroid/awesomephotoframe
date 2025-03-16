package com.example.awesomephotoframe

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.awesomephotoframe.viewmodel.WeatherViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.squareup.picasso.Picasso


class MainActivity : AppCompatActivity(), View.OnClickListener {
    companion object {
        private const val TAG = "MainActivity"
        private const val RC_SIGN_IN = 9001
    }

    private val weatherViewModel: WeatherViewModel by viewModels()
    private lateinit var tvWeather: TextView
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var tvUser: TextView
    private lateinit var tvTime: TextView
    private lateinit var ivPhoto: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvWeather = findViewById(R.id.tv_weather)
        tvUser = findViewById(R.id.tv_user)
        tvTime = findViewById(R.id.tv_time)
        ivPhoto = findViewById(R.id.iv_photo)

        findViewById<View>(R.id.sign_in_button).setOnClickListener(this)
        findViewById<View>(R.id.sign_out_button).setOnClickListener(this)

        // Google Sign-Inの設定
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // 既存のログイン状態を確認
        val account = GoogleSignIn.getLastSignedInAccount(this)
        updateUI(account)

        // 天気情報をロード
        weatherViewModel.loadWeather(35.6895, 139.6917)

        // LiveData を監視 (observe)
        weatherViewModel.weather.observe(this, Observer { weather ->
            weather?.let {
                tvWeather.text = "${it.temperature}°C, ${it.description}"
            }
        })
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            Log.d(TAG, "Sign-in successful: ${account.email}")
            updateUI(account)
        } catch (e: ApiException) {
            Log.w(TAG, "Sign-in failed", e)
            updateUI(null)
        }
    }

    private fun signOut() {
        googleSignInClient.signOut().addOnCompleteListener {
            updateUI(null)
        }
    }

    private fun updateUI(account: GoogleSignInAccount?) {
        if (account != null) {
            tvUser.text = "Welcome, ${account.displayName}"
            account.photoUrl?.let { uri ->
                Picasso.get()
                    .load(uri)
                    .placeholder(android.R.drawable.sym_def_app_icon)
                    .error(android.R.drawable.sym_def_app_icon)
                    .into(ivPhoto)
            }
            findViewById<View>(R.id.sign_in_button).visibility = View.GONE
            findViewById<View>(R.id.sign_out_button).visibility = View.VISIBLE
        } else {
            tvUser.text = "Not signed in"
            ivPhoto.setImageResource(android.R.drawable.sym_def_app_icon)
            findViewById<View>(R.id.sign_in_button).visibility = View.VISIBLE
            findViewById<View>(R.id.sign_out_button).visibility = View.GONE
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.sign_in_button -> signIn()
            R.id.sign_out_button -> signOut()
        }
    }
}