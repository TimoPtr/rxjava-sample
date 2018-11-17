package com.github.timoptr.sample.rxjava.wrapper

import org.eclipse.paho.client.mqttv3.MqttMessage
import org.junit.Test
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author Nibeaudeau Timothy <timothy.nibeaudeau@gmail.com>
 * @version 0.1
 * on 17/11/2018 for rxjava-sample with IntelliJ IDEA.
 */
class RxMqttClientTest {

    @Test
    fun sampleOpenConnectionSendMessage() {
        val topic = "testTopic_${UUID.randomUUID()}"
        val client = RxMqttClient("tcp://test.mosquitto.org:1883", null, null, arrayOf(topic))

        // Just open the wrapper over MQTT send a message and check if we received it and disconnect
        client.connectCompletable()
                .doOnComplete {
                    println("Connected")
                }
                .andThen(client.messageObservable.doOnSubscribe { client.write(RxMqttClient.Message(topic, MqttMessage(byteArrayOf(1)))) })
                .doOnNext {
                    println("Message received")
                }
                .take(1)
                .doFinally {
                    println("Disconnect")
                    client.disconnect()
                }.test().await().assertNoErrors().assertValue { it.topic == topic && byteArrayOf(1).contentEquals(it.mqttMessage.payload) }
    }


    @Test
    fun sampleUseObservableConnection() {
        val topic = "testTopic_${UUID.randomUUID()}"
        val client = RxMqttClient("tcp://test.mosquitto.org:1883", null, null, arrayOf(topic))

        // Just connect by using the observable wrapper, check that the first state is CONNECTED
        client.connectObservable()
                .take(1)
                .doOnNext {
                    println("Connected")
                }
                .doFinally {
                    println("Disconnect")
                    client.disconnect()
                }.test().await().assertNoErrors().assertValue { it == State.CONNECTED }
    }

    @Test
    fun sampleUseObservableConnectionDisconnection() {
        val topic = "testTopic_${UUID.randomUUID()}"
        val client = RxMqttClient("tcp://test.mosquitto.org:1883", null, null, arrayOf(topic))
        val isConnected = AtomicBoolean(false)
        // Just connect by using the observable wrapper and disconnect, check that the first state is CONNECTED
        client.connectObservable()
                .doOnNext {
                    println(it)
                    if (isConnected.compareAndSet(false, true)) {
                        client.disconnect()
                    }
                }
                .take(2)
                .test().await().assertNoErrors().assertValues(State.CONNECTED, State.DISCONNECTED)
    }
}