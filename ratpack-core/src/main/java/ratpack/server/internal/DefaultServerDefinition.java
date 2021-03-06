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

package ratpack.server.internal;

import ratpack.func.Function;
import ratpack.handling.Handler;
import ratpack.registry.Registry;
import ratpack.server.RatpackServer;
import ratpack.server.ServerConfig;

public final class DefaultServerDefinition implements RatpackServer.Definition {

  private final ServerConfig serverConfig;
  private final Registry userRegistry;
  private final Function<? super Registry, ? extends Handler> handlerFactory;

  public DefaultServerDefinition(ServerConfig serverConfig, Registry userRegistry, Function<? super Registry, ? extends Handler> handlerFactory) {
    this.serverConfig = serverConfig;
    this.userRegistry = userRegistry;
    this.handlerFactory = handlerFactory;
  }

  @Override
  public ServerConfig getServerConfig() {
    return serverConfig;
  }

  @Override
  public Registry getUserRegistry() {
    return userRegistry;
  }

  @Override
  public Function<? super Registry, ? extends Handler> getHandlerFactory() {
    return handlerFactory;
  }

}
