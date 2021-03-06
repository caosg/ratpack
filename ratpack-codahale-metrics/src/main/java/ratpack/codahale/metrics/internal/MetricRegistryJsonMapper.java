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

package ratpack.codahale.metrics.internal;

import com.codahale.metrics.*;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.google.inject.Inject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.func.Function;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

public class MetricRegistryJsonMapper implements Function<MetricRegistry, ByteBuf> {

  private final static Logger LOGGER = LoggerFactory.getLogger(MetricRegistryJsonMapper.class);
  private final static TimeUnit DEFAULT_RATE_UNIT = TimeUnit.SECONDS;
  private final static TimeUnit DEFAULT_DURATION_UNIT = TimeUnit.MILLISECONDS;

  private final JsonFactory factory = new JsonFactory();
  private final Clock clock = Clock.defaultClock();
  private final double durationFactor;
  private final double rateFactor;
  private final ByteBufAllocator byteBufAllocator;

  @Inject
  public MetricRegistryJsonMapper(ByteBufAllocator byteBufAllocator) {
    this.byteBufAllocator = byteBufAllocator;
    this.durationFactor = 1.0 / DEFAULT_DURATION_UNIT.toNanos(1);
    this.rateFactor = DEFAULT_RATE_UNIT.toSeconds(1);
  }

  @Override
  public ByteBuf apply(MetricRegistry metricRegistry) throws Exception {
    ByteBuf byteBuf = byteBufAllocator.ioBuffer();
    try {
      OutputStream out = new ByteBufOutputStream(byteBuf);
      JsonGenerator json = factory.createGenerator(out);

      json.writeStartObject();
      json.writeNumberField("timestamp", clock.getTime());
      writeTimers(json, metricRegistry.getTimers());
      writeGauges(json, metricRegistry.getGauges());
      writeMeters(json, metricRegistry.getMeters());
      writeCounters(json, metricRegistry.getCounters());
      writeHistograms(json, metricRegistry.getHistograms());
      json.writeEndObject();

      json.flush();
      json.close();

      return byteBuf;
    } catch (Exception e) {
      byteBuf.release();
      throw e;
    }
  }

  private void writeHistograms(JsonGenerator json, SortedMap<String, Histogram> histograms) throws IOException {
    json.writeArrayFieldStart("histograms");
    for (Map.Entry<String, Histogram> entry : histograms.entrySet()) {
      Histogram histogram = entry.getValue();

      json.writeStartObject();
      json.writeStringField("name", entry.getKey());
      json.writeNumberField("count", histogram.getCount());

      Snapshot snapshot = histogram.getSnapshot();
      json.writeNumberField("min", convertDuration(snapshot.getMin()));
      json.writeNumberField("max", convertDuration(snapshot.getMax()));
      json.writeNumberField("mean", convertDuration(snapshot.getMean()));
      json.writeNumberField("stdDev", convertDuration(snapshot.getStdDev()));
      json.writeNumberField("50thPercentile", convertDuration(snapshot.getMedian()));
      json.writeNumberField("75thPercentile", convertDuration(snapshot.get75thPercentile()));
      json.writeNumberField("95thPercentile", convertDuration(snapshot.get95thPercentile()));
      json.writeNumberField("98thPercentile", convertDuration(snapshot.get98thPercentile()));
      json.writeNumberField("99thPercentile", convertDuration(snapshot.get99thPercentile()));
      json.writeNumberField("999thPercentile", convertDuration(snapshot.get999thPercentile()));
      json.writeEndObject();
    }
    json.writeEndArray();
  }

  private void writeCounters(JsonGenerator json, SortedMap<String, Counter> counters) throws IOException {
    json.writeArrayFieldStart("counters");
    for (Map.Entry<String, Counter> entry : counters.entrySet()) {
      Counter counter = entry.getValue();

      json.writeStartObject();
      json.writeStringField("name", entry.getKey());
      json.writeNumberField("count", counter.getCount());
      json.writeEndObject();
    }
    json.writeEndArray();
  }

  private void writeMeters(JsonGenerator json, SortedMap<String, Meter> meters) throws IOException {
    json.writeArrayFieldStart("meters");
    for (Map.Entry<String, Meter> entry : meters.entrySet()) {
      Meter meter = entry.getValue();

      json.writeStartObject();
      json.writeStringField("name", entry.getKey());
      json.writeNumberField("count", meter.getCount());
      json.writeNumberField("meanRate", convertRate(meter.getMeanRate()));
      json.writeNumberField("oneMinuteRate", convertRate(meter.getOneMinuteRate()));
      json.writeNumberField("fiveMinuteRate", convertRate(meter.getFiveMinuteRate()));
      json.writeNumberField("fifteenMinuteRate", convertRate(meter.getFifteenMinuteRate()));
      json.writeEndObject();
    }
    json.writeEndArray();
  }

  private void writeTimers(JsonGenerator json, SortedMap<String, Timer> timers) throws IOException {
    json.writeArrayFieldStart("timers");
    for (Map.Entry<String, Timer> entry : timers.entrySet()) {
      Timer timer = entry.getValue();

      json.writeStartObject();
      json.writeStringField("name", entry.getKey());
      json.writeNumberField("count", timer.getCount());
      json.writeNumberField("meanRate", convertRate(timer.getMeanRate()));
      json.writeNumberField("oneMinuteRate", convertRate(timer.getOneMinuteRate()));
      json.writeNumberField("fiveMinuteRate", convertRate(timer.getFiveMinuteRate()));
      json.writeNumberField("fifteenMinuteRate", convertRate(timer.getFifteenMinuteRate()));

      Snapshot snapshot = timer.getSnapshot();
      json.writeNumberField("min", convertDuration(snapshot.getMin()));
      json.writeNumberField("max", convertDuration(snapshot.getMax()));
      json.writeNumberField("mean", convertDuration(snapshot.getMean()));
      json.writeNumberField("stdDev", convertDuration(snapshot.getStdDev()));
      json.writeNumberField("50thPercentile", convertDuration(snapshot.getMedian()));
      json.writeNumberField("75thPercentile", convertDuration(snapshot.get75thPercentile()));
      json.writeNumberField("95thPercentile", convertDuration(snapshot.get95thPercentile()));
      json.writeNumberField("98thPercentile", convertDuration(snapshot.get98thPercentile()));
      json.writeNumberField("99thPercentile", convertDuration(snapshot.get99thPercentile()));
      json.writeNumberField("999thPercentile", convertDuration(snapshot.get999thPercentile()));
      json.writeEndObject();
    }
    json.writeEndArray();
  }

  @SuppressWarnings("rawtypes")
  private void writeGauges(JsonGenerator json, SortedMap<String, Gauge> gauges) throws IOException {
    json.writeArrayFieldStart("gauges");
    for (Map.Entry<String, Gauge> entry : gauges.entrySet()) {
      Gauge gauge = entry.getValue();

      json.writeStartObject();
      json.writeStringField("name", entry.getKey());
      try {
        json.writeFieldName("value");
        json.writeObject(gauge.getValue());
      } catch (Exception e) {
        LOGGER.debug("Exception encountered while reporting [" + entry.getKey() + "]: " + e.getLocalizedMessage());
        json.writeNull();
      }
      json.writeEndObject();

    }
    json.writeEndArray();
  }

  private double convertDuration(double duration) {
    return duration * durationFactor;
  }

  private double convertRate(double rate) {
    return rate * rateFactor;
  }

}
