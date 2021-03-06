/*
 * Copyright 2013 the original author or authors.
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

package ratpack.groovy.internal;

import groovy.lang.Closure;
import groovy.lang.Script;
import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;
import ratpack.func.Action;
import ratpack.func.Factory;
import ratpack.func.Function;
import ratpack.groovy.script.internal.ScriptEngine;
import ratpack.handling.Context;
import ratpack.handling.Handler;
import ratpack.reload.internal.ReloadableFileBackedFactory;

import java.nio.file.Path;

import static ratpack.util.ExceptionUtils.uncheck;

public class ScriptBackedApp implements Handler {

  private final Factory<Handler> reloadHandler;
  private final Path script;

  public ScriptBackedApp(Path script, final boolean staticCompile, boolean reloadable, final Function<Closure<?>, Handler> closureTransformer) {
    this.script = script;
    this.reloadHandler = new ReloadableFileBackedFactory<>(script, reloadable, new ReloadableFileBackedFactory.Producer<Handler>() {
      public Handler produce(final Path file, final ByteBuf bytes) {
        try {
          final String string = bytes.toString(CharsetUtil.UTF_8);
          final ScriptEngine<Script> scriptEngine = new ScriptEngine<>(getClass().getClassLoader(), staticCompile, Script.class);

          Runnable runScript = new Runnable() {
            public void run() {
              try {
                scriptEngine.create(file.getFileName().toString(), file, string).run();
              } catch (Exception e) {
                throw uncheck(e);
              }
            }
          };

          ClosureCaptureAction backing = new ClosureCaptureAction();
          RatpackScriptBacking.withBacking(backing, runScript);

          return closureTransformer.apply(backing.closure);
        } catch (Exception e) {
          throw uncheck(e);
        }
      }
    });

    new Thread(new Runnable() {
      public void run() {
        try {
          reloadHandler.create();
        } catch (Exception ignore) {
          // ignore
        }
      }
    }).run();
  }

  public void handle(Context context) throws Exception {
    Handler handler = reloadHandler.create();
    if (handler == null) {
      context.getResponse().send("script file does not exist:" + script.toAbsolutePath());
    } else {
      handler.handle(context);
    }
  }

  private static class ClosureCaptureAction implements Action<Closure<?>> {
    private Closure<?> closure;

    public void execute(Closure<?> configurer) throws Exception {
      closure = configurer;
    }
  }
}
