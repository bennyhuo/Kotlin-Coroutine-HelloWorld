package cn.kotliner.primer.coroutine.common

import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*

/**
 * Created by benny on 5/20/17.
 */
val dateFormat = SimpleDateFormat("HH:mm:ss:SSS")

val now = {
    dateFormat.format(Date(System.currentTimeMillis()))
}

fun log(msg: String) = println("${now()} [${Thread.currentThread().name}] $msg")