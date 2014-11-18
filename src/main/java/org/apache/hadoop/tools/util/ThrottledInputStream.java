/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.tools.util;

import java.io.IOException;
import java.io.InputStream;

public class ThrottledInputStream extends InputStream {

  private final InputStream rawStream;
  private final float maxBytesPerSec;
  private final long startTime = System.currentTimeMillis();
  private static final long MILLISECONDS_IN_SEC = 1000;
  private static final float FLOAT_CONSTANT = 1.0f;

  private long bytesRead = 0;
  private long totalSleepTime = 0;

  private static final long SLEEP_DURATION_MS = 50;

  public ThrottledInputStream(InputStream rawStream) {
    this(rawStream, Long.MAX_VALUE);
  }

  public ThrottledInputStream(InputStream rawStream, float maxBytesPerSec) {
    assert maxBytesPerSec > 0 : "Bandwidth " + maxBytesPerSec + " is invalid"; 
    this.rawStream = rawStream;
    this.maxBytesPerSec = maxBytesPerSec;
  }

  @Override
  public int read() throws IOException {
    throttle();
    int data = rawStream.read();
    if (data != -1) {
      bytesRead++;
    }
    return data;
  }

  @Override
  public int read(byte[] b) throws IOException {
    throttle();
    int readLen = rawStream.read(b);
    if (readLen != -1) {
      bytesRead += readLen;
    }
    return readLen;
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    throttle();
    int readLen = rawStream.read(b, off, len);
    if (readLen != -1) {
      bytesRead += readLen;
    }
    return readLen;
  }

  private void throttle() throws IOException {
    if (getBytesPerSec() > maxBytesPerSec) {
      try {
        Thread.sleep(SLEEP_DURATION_MS);
        totalSleepTime += SLEEP_DURATION_MS;
      } catch (InterruptedException e) {
        throw new IOException("Thread aborted", e);
      }
    }
  }

  public long getTotalBytesRead() {
    return bytesRead;
  }

  public float getBytesPerSec() {
    long elapsedTimeInMilliSecs = System.currentTimeMillis() - startTime;
    if (elapsedTimeInMilliSecs <= MILLISECONDS_IN_SEC) {
      return (bytesRead * FLOAT_CONSTANT);
    } else {
      return (bytesRead * MILLISECONDS_IN_SEC * FLOAT_CONSTANT)/ elapsedTimeInMilliSecs;
    }
  }

  public long getTotalSleepTime() {
    return totalSleepTime;
  }

  @Override
  public void close() throws IOException {
    rawStream.close();
  }

  @Override
  public String toString() {
    return "ThrottledInputStream{" +
        "bytesRead=" + bytesRead +
        ", maxBytesPerSec=" + maxBytesPerSec +
        ", bytesPerSec=" + getBytesPerSec() +
        ", totalSleepTime=" + totalSleepTime +
        '}';
  }
}
