package com.pdevice.handler;

import android.util.Log;

public class Pointer {
    //private final ServiceManager serviceManager = new ServiceManager();
    private int scaledX;
    private int scaledY;
    private int keyCode;
    private int hScroll;
    private int vScroll;
    private int swipeX;
    private int swipeY;
    public Pointer(){

    }
    public void parseCoordinatesClick(String data, int rotation, int width, int height) {
        try {
            String[] arrOfStr = data.split(" ");
            scaledX = Integer.parseInt(arrOfStr[1]);
            scaledY = Integer.parseInt(arrOfStr[2]);
            Log.i("SystemCapture", "Portrait Mode X :  "+scaledX + " Y : "+  scaledY + " WIDTH : "+ width+ " HEIGHT : "+ height);

            if(rotation == 1 ){
                int temp = scaledX;
                scaledX = scaledY;
                scaledY = height-temp;
            }
            Log.i("SystemCapture", "Landscape Mode X :  "+scaledX + " Y : "+  scaledY + " WIDTH : "+ width+ " HEIGHT : "+ height);

        } catch(Exception ex){
            Log.v("pCloudy Touch Events ", ex.toString());
        }
    }

    public void parseCoordinatesSwipe(String data, int rotation, int width, int height) {
        try {
            String[] arrOfStr = data.split(" ");
            scaledX = Integer.parseInt(arrOfStr[1]);
            scaledY = Integer.parseInt(arrOfStr[2]);
            swipeX  = Integer.parseInt(arrOfStr[3]);
            swipeY  = Integer.parseInt(arrOfStr[4]);
            Log.i("SystemCapture", "Portrait Mode X :  "+scaledX + " Y : "+  scaledY + " WIDTH : "+ width+ " HEIGHT : "+ height);

            if(rotation == 1 ){
                int temp = scaledX;
                scaledX = scaledY;
                scaledY = height-temp;
            }
            Log.i("SystemCapture", "Landscape Mode X :  "+scaledX + " Y : "+  scaledY + " WIDTH : "+ width+ " HEIGHT : "+ height);

        } catch(Exception ex){
            Log.v("pCloudy Touch Events ", ex.toString());
        }
    }


    public void parseCoordinatesScrolling(String data, int rotation, int width, int height) {
        try {
            String[] arrOfStr = data.split(" ");
            scaledX = Integer.parseInt(arrOfStr[1]);
            scaledY = Integer.parseInt(arrOfStr[2]);
            Log.i("SystemCapture", "Portrait Mode X :  "+scaledX + " Y : "+  scaledY + " WIDTH : "+ width+ " HEIGHT : "+ height);

            if(rotation == 1 ){
                int temp = scaledX;
                scaledX = scaledY;
                scaledY = height-temp;
            }
            Log.i("SystemCapture", "Landscape Mode X :  "+scaledX + " Y : "+  scaledY + " WIDTH : "+ width+ " HEIGHT : "+ height);
        } catch(Exception ex){
            Log.v("pCloudy Scroll Events ", ex.toString());
        }
    }

    public void Landscap_parseCoordinatesClick(int Height, int Width, String data) {
        try {
            String[] arrOfStr = data.split(" ");
            scaledX = Integer.parseInt(arrOfStr[1]);
            scaledY = Integer.parseInt(arrOfStr[2]);

            scaledX = scaledY;
            scaledY = Width - scaledX;
            //xPoint = py;
            //yPoint = cl->screen->width - px;
        } catch(Exception ex){
            System.err.println("pCloudy Landscap_parseCoordinatesClick exception "+ ex.toString());
        }
    }


    public boolean parseKey(String data) {
        String[] arrOfStr = data.split(" ");
        data = arrOfStr[1];

        boolean value = true;
        try {
            if(data.equals("shift")) {
                keyCode = 59;
            } else if(data.equals("TAB")) {
                keyCode = 61;
            } else if(data.equals("ENTER")) {
                keyCode = 66;
            } else if (data.equals("BACKSPACE")) {
                keyCode = 67;
            } else if (data.equals("DEL")) {
                keyCode = 112;
            } else if (data.equals("UP_ARROW")) {
                keyCode = 19;
            } else if (data.equals("DOWN_ARROW")) {
                keyCode = 20;
            } else if (data.equals("LEFT_ARROW")) {
                keyCode = 21;
            } else if (data.equals("RIGHT_ARROW")) {
                keyCode = 22;
            } else if (data.equals("HOME")) {
                keyCode = 3;
            } else if (data.equals("MENU")) {
                keyCode = 187;
            } else if (data.equals("BACK")) {
                keyCode = 4;
            } else if (data.equals("SHIFT")) {
                keyCode = 59;
            } else if (data.equals("CAPS")) {
                keyCode = 20;
            }  else if (data.equals("ESCAPE")) {
                keyCode = 27;
            } else if (data.equals("SPACE")) {
                keyCode = 62;
            } else if (data.equals("SLASH")) {
                keyCode = 73;
            } else if (data.equals("APOSTROPHE")) {
                keyCode = 96;
            }else {
                value = false;
            }
        }
        catch(Exception ex){
            Log.v("pCloudy Key Events ", ex.toString());
        }
        return value;
    }

    public int getX() {
        return scaledX;
    }

    public int getY() {
        return scaledY;
    }

    public int getHScroll(){return hScroll;}

    public int getVScroll(){return vScroll;}

    public int getActionUP(){
        return 1;
    }

    public int getActionDown(){
        return 0;
    }

    public int getSwipeX(){
        return swipeX;
    }

    public int getSwipeY(){
        return swipeY;
    }

    public int getSwipeAction(){
        return 2;
    }

    public int getKeyCode() {
        return keyCode;
    }



}
