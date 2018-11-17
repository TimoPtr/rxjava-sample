package com.github.timoptr.sample.rxjava.wrapper

import org.junit.Test

/**
 * @author Nibeaudeau Timothy <timothy.nibeaudeau@gmail.com>
 * @version 0.1
 * on 17/11/2018 for rxjava-sample with IntelliJ IDEA.
 */
class RxWebSocketClientTest {

    @Test
    fun sampleOpenConnectionSendMessage() {
        val client = RxWebSocketClient("ws://echo.websocket.org")
        val message = "helloworld"

        // Just open a connection send a message on an echo server check the result it has to be the same as the one send
        client.connectObservable()
                .flatMap {
                    if (it == State.CONNECTED) {
                        println("CONNECTED")
                        client.message.doOnSubscribe { client.write(message) }
                    } else {
                        throw InterfaceNotConnectedException("")
                    }
                }
                .doOnNext {
                    println(it)
                }
                .take(1)
                .doFinally {
                    println("DISCONNECT")
                    client.disconnect()
                }.test().await().assertNoErrors().assertValue(message)
    }

}