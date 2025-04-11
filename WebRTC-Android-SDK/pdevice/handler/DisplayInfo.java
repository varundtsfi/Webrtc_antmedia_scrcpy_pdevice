package com.pdevice.handler;

public final class DisplayInfo {
    private final Size size;
    private final int rotation;

    public DisplayInfo(Size size, int rotation) {
        this.size = size;
        this.rotation = rotation;
    }

    public Size getSize() {
        return size;
    }

 }