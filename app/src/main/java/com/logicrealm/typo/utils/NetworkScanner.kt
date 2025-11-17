package com.logicrealm.typo.utils

import java.net.Inet4Address
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.Socket
import kotlin.concurrent.thread

object NetworkScanner {
    fun getLocalSubnet(): String? {
        val interfaces = NetworkInterface.getNetworkInterfaces()

        while (interfaces.hasMoreElements()) {
            val networkInterface = interfaces.nextElement()
            val addresses = networkInterface.inetAddresses

            while (addresses.hasMoreElements()) {
                val address = addresses.nextElement()
                if (!address.isLoopbackAddress && address is Inet4Address) {
                    val hostAddress = address.hostAddress
                    return hostAddress?.substringBeforeLast(".")
                }
            }
        }
        return null
    }

    fun isPortOpen(ip: String, port: Int, timeout: Int = 200): Boolean {
        return try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(ip, port), timeout)
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun scanLocalNetwork(subnet: String, port: Int): List<String>? {
        val openHosts = mutableListOf<String>()
        val threads = mutableListOf<Thread>()

        for (i in 1..254) {
            val host = "$subnet.$i"
            val thread = thread {
                if (isPortOpen(host, port)) {
                    synchronized(openHosts) {
                        openHosts.add(host)
                    }
                }
            }
            threads.add(thread)
        }

        threads.forEach { it.join() }
        return openHosts
    }
}