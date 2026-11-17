
package com.alsalama.custody

import org.json.JSONObject

data class AnonymousSession(
    val accessToken: String,
    val refreshToken: String,
    val userId: String
)

object SupabaseAuth {

    suspend fun createAnonymousSession(): AnonymousSession? {

        val response = SupabaseHttpClient.post(
            endpoint = "auth/v1/signup",
            body = JSONObject().put(
                "data",
                JSONObject()
            )
        )

        if (!response.isSuccessful) {
            return null
        }

        return try {
            val result = JSONObject(response.body)

            val accessToken = result.optString("access_token")
            val refreshToken = result.optString("refresh_token")
            val userId = result
                .optJSONObject("user")
                ?.optString("id")
                .orEmpty()

            if (
                accessToken.isBlank() ||
                refreshToken.isBlank() ||
                userId.isBlank()
            ) {
                null
            } else {
                AnonymousSession(
                    accessToken = accessToken,
                    refreshToken = refreshToken,
                    userId = userId
                )
            }

        } catch (e: Exception) {
            null
        }
    }
}
