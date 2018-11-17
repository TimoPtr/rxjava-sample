package com.github.timoptr.sample.rxjava.wrapper

import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import okhttp3.*
import java.util.concurrent.TimeUnit

/**
 * @author Nibeaudeau Timothy <timothy.nibeaudeau@gmail.com>
 * @version 0.1
 * on 17/11/2018 for rxjava-sample with IntelliJ IDEA.
 */

/**
 * Reactive wrapper around WebSocket base on okhttp library
 * This wrapper is just a simple implementation
 *
 * @param url of the websocket like wss://echo.websocket.org
 * @param connectionTimeout the timeout of the connection
 * @param connectionPingInterval the interval between pings
 */
class RxWebSocketClient(url: String, connectionTimeout: Long = 3, connectionPingInterval: Long = 10) {
    private val client = OkHttpClient.Builder()
            .connectTimeout(connectionTimeout, TimeUnit.SECONDS)
            .pingInterval(connectionPingInterval, TimeUnit.SECONDS)
            .build()

    private var request = Request.Builder().url(url).build()
    private var ws: WebSocket? = null

    /**
     * The wrapper state of client to listen to get the status of the wrapper
     */
    private val connectionSubject = BehaviorSubject.createDefault(State.DISCONNECTED)
    val connectionState: Observable<State> = connectionSubject

    /**
     * Listen to this [Observable] to get the message from the WebSocket once it's [State.CONNECTED]
     */
    private val readSubject: PublishSubject<String> = PublishSubject.create()
    val message: Observable<String> = readSubject


    /**
     * Open the WebSocket connection
     * To disconnect call [disconnect] it will close the connection
     *
     * @return an [Observable] on the state of the wrapper it emit [State.CONNECTED] when the websocket client is connected
     */
    fun connectObservable(): Observable<State> =
            if (ws == null) {
                Observable.create { emitter ->
                    ws = client.newWebSocket(request, object : WebSocketListener() {
                        override fun onOpen(webSocket: WebSocket, response: Response) {
                            super.onOpen(webSocket, response)
                            if (emitter != null && !emitter.isDisposed) {
                                emitter.onNext(State.CONNECTED)
                            }
                            connectionSubject.onNext(State.CONNECTED)
                        }

                        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                            super.onFailure(webSocket, t, response)
                            if (emitter != null && !emitter.isDisposed) {
                                emitter.tryOnError(t)
                            }
                        }

                        override fun onMessage(webSocket: WebSocket, text: String) {
                            super.onMessage(webSocket, text)
                            readSubject.onNext(text)
                        }

                        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                            super.onClosed(webSocket, code, reason)
                            if (emitter != null && !emitter.isDisposed) {
                                emitter.onNext(State.DISCONNECTED)
                            }
                            connectionSubject.onNext(State.DISCONNECTED)
                            ws = null
                        }
                    })
                }
            } else {
                connectionSubject
            }

    /**
     * Send data over WebSocket
     * @throws InterfaceNotConnectedException if the mqtt isn't connected
     */
    fun write(data: String) {
        ws?.send(data) ?: throw InterfaceNotConnectedException("Mqtt client not connected")
    }

    /**
     * Disconnect from websocket
     */
    fun disconnect() {
        ws?.close(1000, "complete")
    }
}