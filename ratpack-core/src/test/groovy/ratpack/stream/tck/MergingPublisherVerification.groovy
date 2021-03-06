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

package ratpack.stream.tck

import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import org.reactivestreams.tck.PublisherVerification
import org.reactivestreams.tck.TestEnvironment
import ratpack.stream.Streams

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

class MergingPublisherVerification extends PublisherVerification<Integer> {

  public static final long DEFAULT_TIMEOUT_MILLIS = 300L
  public static final long PUBLISHER_REFERENCE_CLEANUP_TIMEOUT_MILLIS = 1000L

  MergingPublisherVerification() {
    super(new TestEnvironment(DEFAULT_TIMEOUT_MILLIS), PUBLISHER_REFERENCE_CLEANUP_TIMEOUT_MILLIS)
  }

  ScheduledExecutorService scheduled = Executors.newSingleThreadScheduledExecutor()

  @Override
  Publisher<Integer> createPublisher(long elements) {
    Streams.merge(
      new Publisher<Iterable<Integer>>() {
        @Override
        void subscribe(Subscriber<? super Iterable<Integer>> s) {
          s.onSubscribe(new Subscription() {
            @Override
            void request(long n) {
              for(int i = 1; i <= elements; i++){
                if( i % 2 != 0){
                  s.onNext(i)
                }
              }

              s.onComplete()
            }

            @Override
            void cancel() { }
          })
        }
      },
      new Publisher<Iterable<Integer>>() {
        @Override
        void subscribe(Subscriber<? super Iterable<Integer>> s) {
          s.onSubscribe(new Subscription() {
            @Override
            void request(long n) {
              for(int i = 1; i <= elements; i++){
                if( i % 2 == 0){
                  s.onNext(i)
                }
              }

              s.onComplete()
            }

            @Override
            void cancel() { }
          })
        }
      }
    )
  }

  @Override
  Publisher<Integer> createErrorStatePublisher() {
    null
  }
}
