/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ratpack.config

import ratpack.server.ServerConfig

class PropsConfigurationSpec extends BaseConfigurationSpec {
  def "supports properties"() {
    def baseDir = tempFolder.newFolder("baseDir").toPath()
    def keyStoreFile = tempFolder.newFile("keystore.jks").toPath()
    def keyStorePassword = "changeit"
    createKeystore(keyStoreFile, keyStorePassword)
    def configFile = tempFolder.newFile("file.properties").toPath()
    configFile.text = """
    |# This is a comment
    |baseDir: ${baseDir.toString().replaceAll("\\\\", "/")}
    |port: 8080
    |address: localhost
    |development: true
    |threads: 3
    |publicAddress: http://localhost:8080
    |maxContentLength: 50000
    |timeResponses: true
    |compressResponses: true
    |compressionMinSize: 100
    |ssl.keyStorePath: ${keyStoreFile.toString().replaceAll("\\\\", "/")}
    |ssl.keyStorePassword: ${keyStorePassword}
    |other.a: 1
    |other.b: 2
    |""".stripMargin()

    when:
    def serverConfig = Configurations.config().props(configFile).build().get(ServerConfig)

    then:
    serverConfig.hasBaseDir
    serverConfig.baseDir.file == baseDir
    serverConfig.port == 8080
    serverConfig.address == InetAddress.getByName("localhost")
    serverConfig.development
    serverConfig.threads == 3
    serverConfig.publicAddress == URI.create("http://localhost:8080")
    serverConfig.maxContentLength == 50000
    serverConfig.timeResponses
    serverConfig.compressResponses
    serverConfig.compressionMinSize == 100
    // TODO: support for lists
//        serverConfig.compressionMimeTypeWhiteList == ["application/json", "text/plain"] as Set
//        serverConfig.compressionMimeTypeBlackList == ["image/png", "image/gif"] as Set
//        serverConfig.indexFiles == ["index.html", "index.htm"]
    serverConfig.SSLContext
    serverConfig.getOtherPrefixedWith("") == [a:"1", b:"2"]
  }

  @SuppressWarnings(["UnnecessaryObjectReferences"])
  def "supports system properties"() {
    def baseDir = tempFolder.newFolder("baseDir").toPath()
    def keyStoreFile = tempFolder.newFile("keystore.jks").toPath()
    def keyStorePassword = "changeit"
    createKeystore(keyStoreFile, keyStorePassword)
    System.setProperty("ratpack.baseDir", baseDir.toString())
    System.setProperty("ratpack.port", "8080")
    System.setProperty("ratpack.address", "localhost")
    System.setProperty("ratpack.development", "true")
    System.setProperty("ratpack.threads", "3")
    System.setProperty("ratpack.publicAddress", "http://localhost:8080")
    System.setProperty("ratpack.maxContentLength", "50000")
    System.setProperty("ratpack.timeResponses", "true")
    System.setProperty("ratpack.compressResponses", "true")
    System.setProperty("ratpack.compressionMinSize", "100")
    System.setProperty("ratpack.ssl.keyStorePath", keyStoreFile.toString())
    System.setProperty("ratpack.ssl.keyStorePassword", keyStorePassword)
    System.setProperty("ratpack.other.a", "1")
    System.setProperty("ratpack.other.b", "2")

    when:
    def serverConfig = Configurations.config().sysProps().build().get(ServerConfig)

    then:
    serverConfig.hasBaseDir
    serverConfig.baseDir.file == baseDir
    serverConfig.port == 8080
    serverConfig.address == InetAddress.getByName("localhost")
    serverConfig.development
    serverConfig.threads == 3
    serverConfig.publicAddress == URI.create("http://localhost:8080")
    serverConfig.maxContentLength == 50000
    serverConfig.timeResponses
    serverConfig.compressResponses
    serverConfig.compressionMinSize == 100
    // TODO: support for lists
//        serverConfig.compressionMimeTypeWhiteList == ["application/json", "text/plain"] as Set
//        serverConfig.compressionMimeTypeBlackList == ["image/png", "image/gif"] as Set
//        serverConfig.indexFiles == ["index.html", "index.htm"]
    serverConfig.SSLContext
    serverConfig.getOtherPrefixedWith("") == [a:"1", b:"2"]
  }
}
