package com.pdevice.handler.compat;

import android.annotation.SuppressLint;
import android.os.IBinder;
import android.os.IInterface;

import java.lang.reflect.Method;

@SuppressLint("PrivateApi")
public final class ServiceManager {
    private static Method getServiceMethod;

    private static DisplayManager displayManager;
    private static InputManager inputManager;

    public ServiceManager() {
        try {
            getServiceMethod = Class.forName("android.os.ServiceManager").getDeclaredMethod("getService", String.class);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private static IInterface getService(String service, String type) {
        try {
            IBinder binder = (IBinder) getServiceMethod.invoke(null, service);
            Method asInterfaceMethod = Class.forName(type + "$Stub").getMethod("asInterface", IBinder.class);
            return (IInterface) asInterfaceMethod.invoke(null, binder);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public static DisplayManager getDisplayManager() {
            if (displayManager == null) {
                displayManager = new DisplayManager(ServiceManager.getService("display", "android.hardware.display.IDisplayManager"));
            }
            return displayManager;
    }

    public InputManager getInputManager() {
        if (inputManager == null) {
            inputManager = new InputManager(getService("input", "android.hardware.input.IInputManager"));
        }

        return inputManager;
    }
}
