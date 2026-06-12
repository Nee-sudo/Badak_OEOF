package com.example.data.api

import android.util.Log
import okhttp3.*
import org.json.JSONObject
import java.util.concurrent.TimeUnit

interface OeofWebSocketListener {
    fun onConnected()
    fun onDisconnected()
    fun onMessageReceived(senderId: String, content: String, roomId: String)
    fun onMessageAck(messageId: String, roomId: String, status: String)
}

class OeofWebSocketClient(
    private val serverUrl: String,
    private val userId: String,
    private val listener: OeofWebSocketListener
) {
    private var client: OkHttpClient? = null
    private var webSocket: WebSocket? = null
    private var isConnected = false

    fun connect() {
        if (isConnected) return

        client = OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .build()

        val request = Request.Builder()
            .url(serverUrl)
            .build()

        webSocket = client?.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                isConnected = true
                Log.d("OeofWS", "WebSocket channel opened. Authenticating @$userId...")
                // Authenticate immediately upon connection
                authenticate(userId)
                listener.onConnected()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("OeofWS", "Received websocket text: $text")
                try {
                    val json = JSONObject(text)
                    when (json.optString("type")) {
                        "auth_ack" -> {
                            Log.d("OeofWS", "Server authenticated citizen token.")
                        }
                        "message" -> {
                            val msgObj = json.optJSONObject("message")
                            if (msgObj != null) {
                                val senderId = msgObj.optString("senderId")
                                val content = msgObj.optString("content")
                                val roomId = msgObj.optString("roomId")
                                listener.onMessageReceived(senderId, content, roomId)
                            }
                        }
                        "msg_ack" -> {
                            val msgId = json.optString("messageId")
                            val rId = json.optString("roomId")
                            val status = json.optString("status")
                            listener.onMessageAck(msgId, rId, status)
                        }
                        "error" -> {
                            Log.e("OeofWS", "Server WebSocket Error: ${json.optString("message")}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("OeofWS", "Error parsing WebSocket incoming message", e)
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("OeofWS", "WebSocket closing: $reason")
                isConnected = false
                listener.onDisconnected()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("OeofWS", "WebSocket Failure: ${t.message}", t)
                isConnected = false
                listener.onDisconnected()
            }
        })
    }

    private fun authenticate(userId: String) {
        val payload = JSONObject().apply {
            put("type", "auth")
            put("userId", userId)
        }
        webSocket?.send(payload.toString())
    }

    fun sendMessage(roomId: String, recipientId: String, content: String) {
        val payload = JSONObject().apply {
            put("type", "message")
            put("roomId", roomId)
            put("recipientId", recipientId)
            put("content", content)
        }
        val sent = webSocket?.send(payload.toString()) ?: false
        Log.d("OeofWS", "Sending WS Message (status=$sent): $payload")
    }

    fun disconnect() {
        webSocket?.close(1000, "User logout")
        webSocket = null
        isConnected = false
    }
}
