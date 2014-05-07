/*
 * Copyright 2012-2013 eBay Software Foundation and ios-driver committers
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.libimobiledevice.ios.driver.binding.services;

import com.sun.jna.ptr.PointerByReference;

import org.libimobiledevice.ios.driver.binding.exceptions.SDKException;
import org.libimobiledevice.ios.driver.binding.raw.ImobiledeviceSdkLibrary;

import java.nio.IntBuffer;

import static org.libimobiledevice.ios.driver.binding.exceptions.SDKErrorCode.throwIfNeeded;
import static org.libimobiledevice.ios.driver.binding.raw.ImobiledeviceSdkLibrary.screenshot_service_free;
import static org.libimobiledevice.ios.driver.binding.raw.ImobiledeviceSdkLibrary.screenshot_service_new;
import static org.libimobiledevice.ios.driver.binding.raw.ImobiledeviceSdkLibrary.screenshot_service_take_screenshot;
import static org.libimobiledevice.ios.driver.binding.raw.ImobiledeviceSdkLibrary.sdk_idevice_screenshot_service_t;


public class ScreenshotService {

  private final ImobiledeviceSdkLibrary.sdk_idevice_screenshot_service_t service;

  public ScreenshotService(IOSDevice d) throws SDKException {
    InformationService info = new InformationService(d);
    boolean dev = info.isDevModeEnabled();
    info.free();
    if (!dev) {
      throw new SDKException(
          "to use screenshots on a device, you need the device to be in dev mode.");
    }
    PointerByReference ptr = new PointerByReference();
    throwIfNeeded(screenshot_service_new(d.getSDKHandle(), ptr));
    service = new sdk_idevice_screenshot_service_t(ptr.getValue());
  }

  public void free() throws SDKException {
    throwIfNeeded(screenshot_service_free(service));
  }

  public byte[] takeScreenshot() throws SDKException {

    PointerByReference ptr = new PointerByReference();
    IntBuffer sizeptr = IntBuffer.allocate(1);

    int res = screenshot_service_take_screenshot(service, ptr, sizeptr);

    if (ptr == null) {
      throw new SDKException("Bug ? pointer should have been assigned by the screenshot service");
    } else if (ptr.getValue() == null) {
      throw new SDKException("Didn't get a value back. Something wrong in screenshot_service");
    } else {
      int size = sizeptr.get(0);
      byte[] b = ptr.getValue().getByteArray(0, size);
      return b;
    }
  }
}
