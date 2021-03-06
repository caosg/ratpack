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

package ratpack.test.http;

import ratpack.api.Nullable;
import ratpack.func.Action;
import ratpack.http.client.RequestSpec;
import ratpack.test.ApplicationUnderTest;
import ratpack.test.http.internal.DefaultTestHttpClient;

public abstract class TestHttpClients {

  /**
   *
   * @param applicationUnderTest Which Ratpack application to make requests against.
   * @return {@link ratpack.test.http.TestHttpClient} which is configured to make requests against the provided ApplicationUnderTest
   */
  public static TestHttpClient testHttpClient(ApplicationUnderTest applicationUnderTest) {
    return testHttpClient(applicationUnderTest, null);
  }

  /**
   *
   * @param applicationUnderTest Which Ratpack application to make requests against.
   * @param requestConfigurer A {@link ratpack.func.Action} that will set up the {@link ratpack.http.client.RequestSpec} for all requests made through this instance of TestHttpClient. These settings can be overridden on a per request basis via {@link ratpack.test.http.TestHttpClient#requestSpec}.
   * @return {@link ratpack.test.http.TestHttpClient} which is configured to make requests against the provided ApplicationUnderTest
   */
  public static TestHttpClient testHttpClient(ApplicationUnderTest applicationUnderTest, @Nullable Action<? super RequestSpec> requestConfigurer) {
    return new DefaultTestHttpClient(applicationUnderTest, Action.noopIfNull(requestConfigurer));
  }

}
