package com.github.timoptr.sample.rxjava.wrapper

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import java.util.*

/**
 * @author Nibeaudeau Timothy <timothy.nibeaudeau@gmail.com>
 * @version 0.1
 * on 17/11/2018 for rxjava-sample with IntelliJ IDEA.
 */

/**
 * Reactive wrapper around MqttClient base on paho library, for now you cannot add topics after the creation of the client
 * This wrapper is just a simple implementation it should not be use like this in production connectCompletable and disconnect are not Thread safe
 *
 * @param broker the broker complete url sample : tcp://test.mosquitto.org:1883
 * @param username the username to use can be null
 * @param password the password to use can be null
 * @param topics the topics to listen
 * @param qos to use for the given topics
 * @param clientID the clientID to use by default a random one is generated
 */
class RxMqttClient(private val broker: String,
                   private val username: String?,
                   private val password: String?,
                   private val topics: Array<String>,
                   private val qos: IntArray = topics.map { 1 }.toIntArray(),
                   private val clientID: String = "Kotlin_${UUID.randomUUID()}") : MqttCallback {

    data class Message(val topic: String, val mqttMessage: MqttMessage)

    /**
     * The wrapper state of client to listen to get the status of the wrapper
     */
    private val connectionSubject = BehaviorSubject.createDefault(State.DISCONNECTED)
    val connectionState: Observable<State> = connectionSubject

    /**
     * The current Mqtt Client used
     */
    private var mqttClient: MqttClient? = null

    /**
     * The Observable on the message received
     */
    private val messageSubject = PublishSubject.create<Message>()
    val messageObservable: Observable<Message> = messageSubject

    /**
     * Create the Mqtt client, connect to the Mqtt broker, update [connectionState] status,
     * subscribe to all [topics] with the qos [qos]
     * if an error ([MqttException]) occur it will be propagate over the onError of the [Completable]
     *
     * @return a completable which complete when the wrapper is over
     */
    fun connectCompletable(): Completable =
            if (mqttClient == null) {
                Completable.create { emitter ->
                    try {
                        mqttClient = MqttClient(broker, clientID, MemoryPersistence())
                        val connection = MqttConnectOptions()
                        connection.isCleanSession = true
                        connection.userName = username
                        connection.password = password?.toCharArray()
                        mqttClient!!.connect(connection)
                        mqttClient!!.setCallback(this)
                        mqttClient!!.subscribeWithResponse(topics, qos)
                        connectionSubject.onNext(State.CONNECTED)
                        emitter.onComplete()
                    } catch (e: MqttException) {
                        mqttClient = null
                        connectionSubject.onNext(State.DISCONNECTED)
                        emitter.onError(e)
                    }
                }
            } else {
                Completable.complete()
            }

    /**
     * Create the Mqtt client, connectCompletable to the Mqtt broker, update [connectionState] status,
     * subscribe to all [topics] with the qos [qos]
     * if an error ([MqttException]) occur it will be propagate over the onError of the [Observable]
     *
     * @return an [Observable] on the state of the wrapper it emit [State.CONNECTED] when the mqtt client is connected
     */
    fun connectObservable(): Observable<State> =
            if (mqttClient == null) {
                Completable.create { emitter ->
                    try {
                        mqttClient = MqttClient(broker, clientID, MemoryPersistence())
                        val connection = MqttConnectOptions()
                        connection.isCleanSession = true
                        connection.userName = username
                        connection.password = password?.toCharArray()
                        mqttClient!!.connect(connection)
                        mqttClient!!.setCallback(this)
                        mqttClient!!.subscribeWithResponse(topics, qos)
                        connectionSubject.onNext(State.CONNECTED)
                        emitter.onComplete()
                    } catch (e: MqttException) {
                        mqttClient = null
                        connectionSubject.onNext(State.DISCONNECTED)
                        emitter.onError(e)
                    }
                }.andThen(connectionState)
            } else {
                connectionState
            }

    /**
     * Disconnect and close the Mqtt client, after this operation you will need to create a new
     * instance of the client
     */
    fun disconnect() {
        try {
            mqttClient?.disconnect()
            mqttClient?.close()
        } catch (t: MqttException) {
            t.printStackTrace()
        }
        mqttClient = null
        connectionSubject.onNext(State.DISCONNECTED)
    }

    /**
     * Send data over mqttClient to the given topics in message
     *
     * @throws InterfaceNotConnectedException if the mqtt isn't connected
     */
    fun write(message: Message) {
        mqttClient?.publish(message.topic, message.mqttMessage)
                ?: throw InterfaceNotConnectedException("Mqtt client not connected")
    }

    override fun messageArrived(topic: String, message: MqttMessage) {
        messageSubject.onNext(Message(topic, message))
    }

    override fun connectionLost(cause: Throwable) {
        cause.printStackTrace()
        disconnect()
    }

    override fun deliveryComplete(token: IMqttDeliveryToken?) {
    }
}
