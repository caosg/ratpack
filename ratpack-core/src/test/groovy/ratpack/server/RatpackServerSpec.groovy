/*
 * Copyright 2014 the original author or authors.
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

package ratpack.server

import ratpack.handling.Handler
import ratpack.test.ApplicationUnderTest
import spock.lang.Specification

class RatpackServerSpec extends Specification {

  def "start default server"() {
    given:
    def server = RatpackServer.of { spec ->
      spec
        .handler {
        return {} as Handler
      }
    }

    when:
    server.start()

    then:
    server.running
    server.bindPort == 5050

    cleanup:
    if (server.running) {
      server.stop()
    }
  }

  def "start server on port"() {
    given:
    def server = RatpackServer.of { spec ->
      spec
        .config(ServerConfig.noBaseDir().port(5060))
        .handler {
        return {} as Handler
      }
    }

    when:
    server.start()

    then:
    server.running
    server.bindPort == 5060

    cleanup:
    if (server.running) {
      server.stop()
    }
  }

  def "reload server"() {
    given:
    def value = "foo"
    def server = RatpackServer.of {
      it
        .registry { it.add(String, value) }
        .handler { return { it.render it.get(String) } as Handler }
    }
    def client = ApplicationUnderTest.of(server).httpClient

    when:
    server.start()

    then:
    server.isRunning()
    client.text == "foo"

    when:
    value = "bar"
    server.reload()

    then:
    server.isRunning()
    client.text == "bar"
  }
}
