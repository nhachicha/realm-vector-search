package com.mongodb.app.data

import android.util.Log
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

const val API_KEY: String = "REPLACE_ME"

object OpenAiRepository {
    private val retrofit: Retrofit
    private val service: OpenAiService

    init {
        val gson = GsonBuilder().create()
        val logger = HttpLoggingInterceptor().apply {
            level =
                HttpLoggingInterceptor.Level.BODY // Set your desired log level (BODY for full request/response)
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logger)
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl("https://api.openai.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        service = retrofit.create(OpenAiService::class.java)
    }

    suspend fun getEmbedding(input: String): List<EmbeddingData> {
        try {
            val response: EmbeddingResponse = service.getEmbedding(EmbeddingRequest(input))
            return response.data
        } catch (e: HttpException) {
            e.printStackTrace()
            return emptyList()
        }
    }

    suspend fun getCompletion(rag: String, prompt: String): String? {
        return try {
            val messages: List<CompletionMessage> = listOf(
                CompletionMessage(
                    "system",
                    "Answer questions based on the provided context. If you dont know simply say I dont know. $rag"
                ),
                CompletionMessage("user", prompt)
            )

            val request = CompletionRequest(messages = messages)
            val response = withContext(Dispatchers.IO) {
                service.getCompletion(request)
            }
            response.choices.firstOrNull()?.message?.content ?: run {
                Log.e("REALM", "Failed to get completion: Response choices are empty")
                null
            }
        } catch (e: Exception) {
            Log.e("REALM", "Failed to get completion: ${e.message}", e)
            null
        }
    }
}


interface OpenAiService {
    @Headers("Content-Type: application/json", "Authorization: Bearer $API_KEY")
    @POST("v1/embeddings")
    suspend fun getEmbedding(@Body request: EmbeddingRequest): EmbeddingResponse

    @Headers("Content-Type: application/json", "Authorization: Bearer $API_KEY")
    @POST("v1/chat/completions")
    suspend fun getCompletion(@Body request: CompletionRequest): CompletionResponse

}

data class EmbeddingRequest(
    val input: String,
    val model: String = "text-embedding-ada-002"
)

data class EmbeddingResponse(
    val data: List<EmbeddingData>
)

data class EmbeddingData(
    val embedding: List<Float>
)

data class CompletionRequest(
    val model: String = "gpt-3.5-turbo",
    val messages: List<CompletionMessage>

)

data class CompletionMessage(
    val role: String, // system or user
    val content: String
)

data class CompletionChoice(
    val index: Int,
    val message: CompletionMessage
)

data class CompletionResponse(
    val choices: List<CompletionChoice>
)
