/*
 * Copyright (C) 2013 DroidDriver committers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.appium.droiddriver.util;

import android.app.Activity;
import io.appium.droiddriver.exceptions.UnrecoverableException;

/**
 * Static helper methods for retrieving activities.
 */
public class ActivityUtils {
  public interface Supplier<T> {
    /**
     * Retrieves an instance of the appropriate type. The returned object may or
     * may not be a new instance, depending on the implementation.
     *
     * @return an instance of the appropriate type
     */
    T get();
  }

  private static Supplier<Activity> runningActivitySupplier;

  /**
   * Sets the Supplier for the running (a.k.a. resumed or foreground) activity.
   * Called from {@link io.appium.droiddriver.runner.TestRunner}. If a
   * custom runner is used, this method must be called appropriately, otherwise
   * {@link #getRunningActivity} won't work.
   */
  public static synchronized void setRunningActivitySupplier(Supplier<Activity> activitySupplier) {
    runningActivitySupplier = activitySupplier;
  }

  /**
   * Shorthand to {@link #getRunningActivity(long)} with {@code timeoutMillis=30_000}.
   */
  public static Activity getRunningActivity() {
    return getRunningActivity(30_000L);
  }

  /**
   * Waits for idle on main looper, then gets the running (a.k.a. resumed or foreground) activity.
   *
   * @return the currently running activity, or null if no activity has focus.
   */
  public static Activity getRunningActivity(long timeoutMillis) {
    // It's safe to check running activity only when the main looper is idle.
    // If the AUT is in background, its main looper should be idle already.
    // If the AUT is in foreground, its main looper should be idle eventually.
    if (InstrumentationUtils.tryWaitForIdleSync(timeoutMillis)) {
      return getRunningActivityNoWait();
    }
    return null;
  }

  /**
   * Gets the running (a.k.a. resumed or foreground) activity without waiting for idle on main
   * looper.
   *
   * @return the currently running activity, or null if no activity has focus.
   */
  public static synchronized Activity getRunningActivityNoWait() {
    if (runningActivitySupplier == null) {
      throw new UnrecoverableException(
          "If you don't use DroidDriver TestRunner, you need to call"
              + " ActivityUtils.setRunningActivitySupplier appropriately");
    }
    return runningActivitySupplier.get();
  }
}
