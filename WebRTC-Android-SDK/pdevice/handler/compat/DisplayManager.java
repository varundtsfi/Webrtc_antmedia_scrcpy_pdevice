package com.pdevice.handler.compat;

import com.pdevice.handler.DisplayInfo;
import com.pdevice.handler.Size;

import android.os.IInterface;

public final class DisplayManager {
    private final IInterface manager;

    public DisplayManager(IInterface manager) {
        this.manager = manager;
    }

    public DisplayInfo getDisplayInfo() {
        try {
            Object displayInfo = manager.getClass().getMethod("getDisplayInfo", int.class).invoke(manager, 0);

            Class<?> cls = displayInfo.getClass();
            // width and height already take the rotation into account
            int width = cls.getDeclaredField("logicalWidth").getInt(displayInfo);
            int height = cls.getDeclaredField("logicalHeight").getInt(displayInfo);
            int rotation = cls.getDeclaredField("rotation").getInt(displayInfo);
            return new DisplayInfo(new Size(width, height), rotation);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }


    public int getRotationInfo() {
        try {
            Object displayInfo = manager.getClass().getMethod("getDisplayInfo", int.class).invoke(manager, 0);
            Class<?> cls = displayInfo.getClass();
            return cls.getDeclaredField("rotation").getInt(displayInfo);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public int getHeightInfo() {
        try {
            Object displayInfo = manager.getClass().getMethod("getDisplayInfo", int.class).invoke(manager, 0);
            Class<?> cls = displayInfo.getClass();
            return cls.getDeclaredField("logicalHeight").getInt(displayInfo);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public int getWidthInfo() {
        try {
            Object displayInfo = manager.getClass().getMethod("getDisplayInfo", int.class).invoke(manager, 0);
            Class<?> cls = displayInfo.getClass();
            return cls.getDeclaredField("logicalWidth").getInt(displayInfo);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
