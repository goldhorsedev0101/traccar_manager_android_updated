/*
 * Copyright 2016 - 2022 Anton Tananaev (anton@traccar.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:Suppress("DEPRECATION")
package gpslink.traccar.manager

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.webkit.WebViewFragment
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    var pendingEventId: Long? = null

    private fun updateEventId(intent: Intent?) {
        intent?.getStringExtra("eventId")?.let { pendingEventId = it.toLongOrNull() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        updateEventId(intent)
        if (savedInstanceState == null) {
            initContent()
        }
    }

    private fun initContent() {
        if (PreferenceManager.getDefaultSharedPreferences(this).contains(PREFERENCE_URL)) {
            fragmentManager.beginTransaction().add(android.R.id.content, MainFragment()).commit()
        } else {
            // Automatically set default server URL and proceed to MainFragment
            val defaultUrl = "https://track.gpslinkusa.com"
            PreferenceManager.getDefaultSharedPreferences(this)
                .edit().putString(PREFERENCE_URL, defaultUrl).apply()
            fragmentManager.beginTransaction().add(android.R.id.content, MainFragment()).commit()
            
            // Optionally validate server in background (silently, no UI feedback)
            validateServerInBackground(defaultUrl)
        }
    }

    @SuppressLint("StaticFieldLeak")
    private fun validateServerInBackground(serverUrl: String) {
        object : AsyncTask<String, Unit, Boolean>() {
            override fun doInBackground(vararg urls: String): Boolean {
                try {
                    val uri = Uri.parse(urls[0]).buildUpon().appendEncodedPath("api/server").build()
                    var url = uri.toString()
                    var urlConnection: HttpURLConnection? = null
                    for (i in 0 until 5) {
                        val resourceUrl = URL(url)
                        urlConnection = resourceUrl.openConnection() as HttpURLConnection
                        urlConnection.instanceFollowRedirects = false
                        when (urlConnection.responseCode) {
                            HttpURLConnection.HTTP_MOVED_PERM, HttpURLConnection.HTTP_MOVED_TEMP -> {
                                url = urlConnection.getHeaderField("Location")
                                continue
                            }
                        }
                        break
                    }
                    val reader = BufferedReader(InputStreamReader(urlConnection?.inputStream))
                    var line: String?
                    val responseBuilder = StringBuilder()
                    while (reader.readLine().also { line = it } != null) {
                        responseBuilder.append(line)
                    }
                    JSONObject(responseBuilder.toString())
                    return true
                } catch (e: IOException) {
                    Log.w(TAG, "Server validation failed: ${e.message}")
                } catch (e: JSONException) {
                    Log.w(TAG, "Server validation failed: ${e.message}")
                }
                return false
            }

            override fun onPostExecute(result: Boolean) {
                // Validation completed silently - no UI feedback
                // Server URL is already saved, MainFragment is already showing
                if (!result) {
                    Log.w(TAG, "Server validation failed, but continuing with saved URL")
                }
            }
        }.execute(serverUrl)
    }

    companion object {
        const val PREFERENCE_URL = "url"
        private val TAG = MainActivity::class.java.simpleName
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        updateEventId(intent)
        LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(MainFragment.EVENT_EVENT))
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        val fragment = fragmentManager.findFragmentById(android.R.id.content)
        fragment?.onRequestPermissionsResult(requestCode, permissions, grantResults)
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onBackPressed() {
        val fragment = fragmentManager.findFragmentById(android.R.id.content) as? WebViewFragment
        if (fragment?.webView?.canGoBack() == true) {
            fragment.webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
