
package com.alsalama.custody

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

data class SupabaseResponse(
    val isSuccessful: Boolean,
    val statusCode: Int,
    val body: String
)

object SupabaseHttpClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType =
        "application/json; charset=utf-8".toMediaType()

    suspend fun post(
        endpoint: String,
        body: JSONObject,
        accessToken: String? = null
    ): SupabaseResponse = withContext(Dispatchers.IO) {

        val url = SupabaseConfig.PROJECT_URL.trimEnd('/') +
            "/" + endpoint.trimStart('/')

        val requestBuilder = Request.Builder()
            .url(url)
            .post(body.toString().toRequestBody(jsonMediaType))
            .header("apikey", SupabaseConfig.PUBLISHABLE_KEY)
            .header("Accept", "application/json")

        if (!accessToken.isNullOrBlank()) {
            requestBuilder.header(
                "Authorization",
                "Bearer $accessToken"
            )
        }

        val request = requestBuilder.build()

        try {
            client.newCall(request).execute().use { response ->
                SupabaseResponse(
                    isSuccessful = response.isSuccessful,
                    statusCode = response.code,
                    body = response.body?.string().orEmpty()
                )
            }
        } catch (e: IOException) {
            SupabaseResponse(
                isSuccessful = false,
                statusCode = -1,
                body = e.message ?: "Network connection error"
            )
        }
    }
}
