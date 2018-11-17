# Samples for [RxJava2](https://github.com/ReactiveX/RxJava/)

Project base on [kotlin-base-project](https://github.com/TimoPtr/kotlin-base-project)

## DebounceBuffer
This combination of operator allow you to debounce the upstream with a given timeout and emit after another given timeout if no value send.

Use case : A source of ByteArray (for instance a Bluetooth Device which send a bunch of data), and a huge consuming parser (take care of potential lost of data),
this operator will be useful to avoid calling the parser on every bunch of data (the message might be split) but after a certain amount of
time between two reception or after a global timeout if there is no delay between two reception (avoid deadlock).

### Implementation

[DebounceBuffer](app/src/main/kotlin/com/github/timoptr/sample/rxjava/operators/DebounceBuffer.kt)

### Sample

[DebounceBufferSample](app/src/test/kotlin/com/github/timoptr/sample/rxjava/operators/DebounceBufferTest.kt)


## MergeAllowError
This operator merge all the sources and doesn't failed unless all sources emit an error.

Use case : Connect to multiple interface, and you don't care if one of them is disconnected


### Implementation

[MergeAllowError](app/src/main/kotlin/com/github/timoptr/sample/rxjava/operators/MergeAllowError.kt)

### Sample

[MergeAllowErrorSample](app/src/test/kotlin/com/github/timoptr/sample/rxjava/operators/MergeAllowErrorTest.kt)

## Wrapper MQTT
This is a simple wrapper around the paho library which expose Observable

### Implementation 

[RxMqttClient](app/src/main/kotlin/com/github/timoptr/sample/rxjava/wrapper/RxMqttClient.kt)

### Sample 

[RxMqttClientSample](app/src/test/kotlin/com/github/timoptr/sample/rxjava/wrapper/RxMqttClientTest.kt)


## WebSocket

## TCP


## BroadcastReceiver


