package cn.kotliner.meeting

import cn.kotliner.primer.coroutine.common.log
import javafx.application.Application
import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import kotlin.concurrent.thread
import kotlin.coroutines.experimental.*

class HelloWorld : Application() {

    override fun start(primaryStage: Stage) {
        primaryStage.title = "Hello World!"
        val btn = Button()
        btn.text = "Say 'Hello World'"
        btn.onAction = EventHandler<ActionEvent> {
            log("coroutine before")
            launch(AsyncContext()) {
                try {
                    log("coroutine start")
                    val result = longTimeTask(this@HelloWorld::doSthLongTime)
                    log("coroutine end $result")
                } catch(e: Exception) {
                    e.printStackTrace()
                }
            }
            log("coroutine after")
        }

        val root = StackPane()
        root.children.add(btn)
        primaryStage.scene = Scene(root, 300.0, 250.0)
        primaryStage.show()
    }

    fun doSthLongTime(): Int {
        log("long time task start")
        Thread.sleep(1000)
        log("long time task end")
        return 0
    }
}

suspend fun <T> longTimeTask(block: () -> T) = suspendCoroutine<T> {
    continuation ->
    thread {
        try {
            val result = block()
            log(continuation.javaClass.toString())
            continuation.resume(result)
        } catch (e: Exception) {
            continuation.resumeWithException(e)
        }
    }
}

fun launch(context: CoroutineContext = EmptyCoroutineContext, block: suspend () -> Unit) {
    block.startCoroutine(BasicContinuation(context))
}

class BasicContinuation(override val context: CoroutineContext = EmptyCoroutineContext): Continuation<Unit> {

    override fun resume(value: Unit) {

    }

    override fun resumeWithException(exception: Throwable) {

    }

}

class ContinuationUIWrapper<T>(val continuation: Continuation<T>) : Continuation<T> {
    override val context: CoroutineContext = continuation.context

    override fun resume(value: T) {
        Platform.runLater {
            continuation.resume(value)
        }
    }

    override fun resumeWithException(exception: Throwable) {
        Platform.runLater {
            continuation.resumeWithException(exception)
        }

    }

}

class AsyncContext: AbstractCoroutineContextElement(ContinuationInterceptor), ContinuationInterceptor{

    override fun <T> interceptContinuation(continuation: Continuation<T>): Continuation<T> {
        return ContinuationUIWrapper(continuation.context.fold(continuation){
            continuation, element ->
            if(element is ContinuationInterceptor && element != this){
                element.interceptContinuation(continuation)
            }else{
                continuation
            }
        })
    }

}