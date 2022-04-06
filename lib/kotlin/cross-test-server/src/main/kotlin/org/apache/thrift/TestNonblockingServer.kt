/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
@file:OptIn(DelicateCoroutinesApi::class)

package org.apache.thrift

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import org.apache.thrift.server.THsHaServer
import org.apache.thrift.server.TNonblockingServer
import org.apache.thrift.server.TServer
import org.apache.thrift.transport.TNonblockingServerSocket
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import thrift.test.ThriftTestProcessor

private val logger: Logger = LoggerFactory.getLogger("TestNonblockingServer")

fun main(args: Array<String>) {
    try {
        var port = 9090
        var hsha = false
        var i = 0
        while (i < args.size) {
            if (args[i] == "-p") {
                port = args[i++].toInt()
            } else if (args[i] == "-hsha") {
                hsha = true
            }
            i += 1
        }
        // Processor
        val testHandler = TestHandler()
        val testProcessor = ThriftTestProcessor(testHandler, scope = GlobalScope)
        // Transport
        val tServerSocket =
            TNonblockingServerSocket(
                TNonblockingServerSocket.NonblockingAbstractServerSocketArgs().port(port)
            )
        val serverEngine: TServer =
            if (hsha) {
                // HsHa Server
                THsHaServer(THsHaServer.Args(tServerSocket).processor(testProcessor))
            } else {
                logger.info("using non blocking server {}", tServerSocket)
                // Nonblocking Server
                TNonblockingServer(THsHaServer.Args(tServerSocket).processor(testProcessor))
            }
        // Run it
        logger.info("Starting the server on port {}, hsha={}...", port, hsha)
        serverEngine.serve()
    } catch (x: Exception) {
        x.printStackTrace()
    }
    logger.info("done.")
}
