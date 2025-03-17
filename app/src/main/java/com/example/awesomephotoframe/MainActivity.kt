package com.example.awesomephotoframe

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
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
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors


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
    private lateinit var ivPhotoFrame: ImageView
    private val photoUpdateHandler = android.os.Handler()
    private val photoUrls = mutableListOf<String>() // 取得した画像のURLリスト

    // ActivityResultLauncherの定義
    private val activityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleSignInResult(task)
        } else {
            Log.w(TAG, "Sign-in failed")
            updateUI(null)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvWeather = findViewById(R.id.tv_weather)
        tvUser = findViewById(R.id.tv_user)
        tvTime = findViewById(R.id.tv_time)
        ivPhoto = findViewById(R.id.iv_photo)
        ivPhotoFrame = findViewById(R.id.iv_photo_frame)

        findViewById<View>(R.id.sign_in_button).setOnClickListener(this)
        findViewById<View>(R.id.sign_out_button).setOnClickListener(this)


        // Google Sign-Inの設定
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(com.google.android.gms.common.api.Scope("https://www.googleapis.com/auth/photoslibrary.readonly"))
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
        activityResultLauncher.launch(signInIntent) // 修正ポイント
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
            fetchGooglePhotos(account)  // Google Photos API で画像取得
            account.photoUrl?.let { uri ->
                Picasso.get()
                    .load(uri)
                    .placeholder(android.R.drawable.sym_def_app_icon)
                    .error(android.R.drawable.sym_def_app_icon)
                    .into(ivPhoto)  // ログインユーザーの画像を iv_photo にセット
            }
            findViewById<View>(R.id.sign_in_button).visibility = View.GONE
            findViewById<View>(R.id.sign_out_button).visibility = View.VISIBLE
        } else {
            tvUser.text = "Not signed in"
            ivPhoto.setImageResource(android.R.drawable.sym_def_app_icon)
            ivPhotoFrame.setImageResource(android.R.drawable.sym_def_app_icon) // Google Photos の画像もリセット
            findViewById<View>(R.id.sign_in_button).visibility = View.VISIBLE
            findViewById<View>(R.id.sign_out_button).visibility = View.GONE
        }
    }


    private fun fetchGooglePhotos(account: GoogleSignInAccount) {
        account.account?.let { googleAccount ->
            val scopes = "oauth2:https://www.googleapis.com/auth/photoslibrary.readonly"

            Executors.newSingleThreadExecutor().execute {
                try {
                    val accessToken = com.google.android.gms.auth.GoogleAuthUtil.getToken(this, googleAccount, scopes)
                    if (accessToken != null) {
                        Log.d(TAG, "Access Token: $accessToken")
                        requestGooglePhotos(accessToken)  // ここで requestGooglePhotos() を呼ぶ
                    } else {
                        Log.e(TAG, "Failed to get access token")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error getting access token", e)
                }
            }
        }
    }


    private fun requestGooglePhotos(accessToken: String) {
        Executors.newSingleThreadExecutor().execute {
            try {
                val url = URL("https://photoslibrary.googleapis.com/v1/mediaItems?pageSize=10")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Authorization", "Bearer $accessToken")
                connection.setRequestProperty("Content-Type", "application/json")

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    reader.close()

                    val jsonResponse = JSONObject(response.toString())
                    val mediaItems = jsonResponse.optJSONArray("mediaItems")

                    if (mediaItems != null && mediaItems.length() > 0) {
                        photoUrls.clear() // リストをリセット
                        for (i in 0 until mediaItems.length()) {
                            val photoUrl = mediaItems.getJSONObject(i).getString("baseUrl")
                            photoUrls.add(photoUrl) // 画像のURLをリストに追加
                        }
                        Log.d(TAG, "Fetched ${photoUrls.size} photos")

                        // すぐに1枚ランダムで表示
                        runOnUiThread { updatePhotoFrame() }

                        // 10分ごとに更新
                        startPhotoUpdateLoop()
                    } else {
                        Log.e(TAG, "No media items found")
                    }
                } else {
                    Log.e(TAG, "Error fetching Google Photos: HTTP $responseCode")
                }
                connection.disconnect()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch Google Photos", e)
            }
        }
    }

    private val photoUpdateRunnable = object : Runnable {
        override fun run() {
            updatePhotoFrame()
//            photoUpdateHandler.postDelayed(this, 10 * 60 * 1000) // 10分後に再実行
            photoUpdateHandler.postDelayed(this, 10 * 60 * 100)
        }
    }

    private fun startPhotoUpdateLoop() {
        photoUpdateHandler.removeCallbacks(photoUpdateRunnable) // 既存のループをリセット
        photoUpdateHandler.post(photoUpdateRunnable) // ループ開始
    }

    override fun onDestroy() {
        super.onDestroy()
        photoUpdateHandler.removeCallbacks(photoUpdateRunnable) // アプリ終了時に停止
    }


    private fun updatePhotoFrame() {
        if (photoUrls.isNotEmpty()) {
            val randomPhotoUrl = photoUrls.random() // ランダムに1枚選択
            Log.d(TAG, "Displaying photo: $randomPhotoUrl")

            runOnUiThread {
                Picasso.get()
                    .load(randomPhotoUrl)
                    .placeholder(android.R.drawable.sym_def_app_icon)
                    .error(android.R.drawable.sym_def_app_icon)
                    .into(ivPhotoFrame)
            }
        }
    }



    override fun onClick(v: View) {
        when (v.id) {
            R.id.sign_in_button -> signIn()
            R.id.sign_out_button -> signOut()
        }
    }
}