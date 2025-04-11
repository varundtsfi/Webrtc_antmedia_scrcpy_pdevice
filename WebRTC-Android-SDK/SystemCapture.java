package io.antmedia.webrtc_android_sample_app;

import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.genymobile.scrcpy.ConfigurationException;
import com.genymobile.scrcpy.Device;
import com.genymobile.scrcpy.FakeContext;
import com.genymobile.scrcpy.Ln;
import com.genymobile.scrcpy.Options;
import com.genymobile.scrcpy.PointersState;
import com.genymobile.scrcpy.Position;
import com.genymobile.scrcpy.Workarounds;
import com.genymobile.scrcpy.wrappers.ServiceManager;


import com.pdevice.handler.Point;
import com.pdevice.handler.Pointer;
import com.pdevice.handler.compat.InputManager;
import com.pdevice.handler.compat.InputManagerWrapper;
import com.pdevice.handler.compat.ServiceManager;
import org.webrtc.DataChannel;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import de.tavendo.autobahn.WebSocket;
import io.antmedia.webrtcandroidframework.IDataChannelObserver;
import io.antmedia.webrtcandroidframework.IWebRTCClient;
import io.antmedia.webrtcandroidframework.IWebRTCListener;
import io.antmedia.webrtcandroidframework.StreamInfo;
import io.antmedia.webrtcandroidframework.WebRTCClient;
import io.antmedia.webrtcandroidframework.apprtc.CallActivity;

public class SystemCapture implements IWebRTCListener, IDataChannelObserver {
    //public final static String TAG = SystemCapture.class.getSimpleName();
    public final static String TAG = "SystemCapture";
    private static InputManagerWrapper    inputManager;
    private final ServiceManager serviceManager = new ServiceManager();
    private static int deviceId = -1; // KeyCharacterMap.VIRTUAL_KEYBOARD
    private static final int DEFAULT_DEVICE_ID = 0;

    private static final int POINTER_ID_MOUSE = -1;
    private static final int POINTER_ID_VIRTUAL_MOUSE = -3;
    private static KeyCharacterMap keyCharacterMap;
    public String temp  =   null;
    private long lastTouchDown;
    private final static MotionEvent.PointerProperties[] pointerProperties = {new MotionEvent.PointerProperties()};
    private final static MotionEvent.PointerCoords[] pointerCoords = {new MotionEvent.PointerCoords()};

    private final PointersState pointersState = new PointersState();

    private static Pointer pointer;
//    private static Point   point;

    public static boolean isOpened = false;
    private static String tokenId      = "tokenId";
    private static String streamId     = "VARUN123456";
    private static String serverUrl    = "wss://stream-ind.pcloudy.com/WebRTCAppEE/websocket";

    private static Device device;

    static WebSocket.WebSocketConnectionObserver observer = new WebSocket.WebSocketConnectionObserver() {


        @Override
        public void onOpen() {
            isOpened = true;
            Log.i(TAG, "onOpen is called");
        }

        @Override
        public void onClose(WebSocketCloseNotification webSocketCloseNotification, String s) {

        }

        @Override
        public void onTextMessage(String s) {

        }

        @Override
        public void onRawTextMessage(byte[] bytes) {

        }

        @Override
        public void onBinaryMessage(byte[] bytes) {

        }
    };

    public static void main(String[] args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("Arguments are not proper");
        }

        serverUrl   = args[0];
        streamId    = args[1];
        Log.d(TAG, "serverURL "+serverUrl + " --- streamId "+streamId);

        SystemCapture systemCapture = new SystemCapture();

        Workarounds.apply(false, false );

        inputManager = new InputManagerWrapper();
        final int[] rotation = {0};
        pointer  = new Pointer();

        initPointer();

        selectDevice();

        loadKeyCharacterMap();

        HandlerThread thread = new HandlerThread("handler-thread");
        thread.start();
        Handler handler = new Handler(thread.getLooper());

        handler.post(() -> {
            try {
                device = new Device(Options.parse());
                int width = device.getScreenInfo().getVideoSize().getWidth();
                int height = device.getScreenInfo().getVideoSize().getHeight();
                rotation[0] = device.getScreenInfo().getDeviceRotation();
                WebRTCClient webRTCClient = new WebRTCClient(systemCapture, FakeContext.get());
                //webRTCClient.setAudioService(ServiceManager.getAudioManager());

                Intent intent = new Intent();
                intent.putExtra(WebRTCClient.EXTRA_DISPLAY_CAPTURE, true);
                intent.putExtra(CallActivity.EXTRA_DATA_CHANNEL_ENABLED, true);
                intent.putExtra(CallActivity.EXTRA_VIDEO_WIDTH, width);
                intent.putExtra(CallActivity.EXTRA_VIDEO_HEIGHT, height);
                intent.putExtra(CallActivity.EXTRA_VIDEO_BITRATE, 2500);
                Log.i(TAG, "screen width: " + width + " screen height: " + height + " Device Current Rotation "+rotation[0]);
                webRTCClient.setMediaRecorderAudioSource(MediaRecorder.AudioSource.REMOTE_SUBMIX);
                webRTCClient.init(serverUrl, streamId, IWebRTCClient.MODE_PUBLISH, tokenId, intent);
                webRTCClient.setStreamId(streamId);
                webRTCClient.setDataChannelOnly(true);
                webRTCClient.setDataChannelObserver(systemCapture);
                Log.i(TAG, "Unsupported mime");
                webRTCClient.startStream();
            } catch(ConfigurationException ex) {
                StringWriter errors = new StringWriter();
                ex.printStackTrace(new PrintWriter(errors));
                Log.e(TAG, "WebRTC Client Config Exception "+errors.toString());
            }
        });
        Log.i(TAG, "Looping in main call");
        Looper.loop();
        Log.i(TAG, "Leaving main application");
    }

    public static void selectDevice() {
        try {
            deviceId = KeyCharacterMap.class.getDeclaredField("VIRTUAL_KEYBOARD").getInt(KeyCharacterMap.class);
        } catch (NoSuchFieldException e) {
            System.err.println("Falling back to KeyCharacterMap.BUILT_IN_KEYBOARD");
            deviceId = 0;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void loadKeyCharacterMap() {
        keyCharacterMap = KeyCharacterMap.load(deviceId);
    }

    private static void initPointer() {
        MotionEvent.PointerProperties props = pointerProperties[0];
        props.id = 0;
        props.toolType = MotionEvent.TOOL_TYPE_FINGER;

        MotionEvent.PointerCoords coords = pointerCoords[0];
        coords.orientation = 0;
        coords.pressure = 1;
        coords.size = 1;
    }

    private static void setPointerCoords(Point point) {
        //System.err.println("setPointerCoords  X "+ point.getX() +" Y "+ point.getY());
        MotionEvent.PointerCoords coords = pointerCoords[0];
        coords.x = point.getX();
        coords.y = point.getY();
    }

    private InputEvent  injectTouch(int action, int buttons, int xPoint, int yPoint) {
        long now = SystemClock.uptimeMillis();
        if (action == 0) {
            this.lastTouchDown = now;
        }
        setPointerCoords(new Point(xPoint, yPoint));
        MotionEvent event = MotionEvent.obtain(this.lastTouchDown, now, action, 1, this.pointerProperties, this.pointerCoords, 0, buttons, 1.0f, 1.0f, 0, 0, InputDevice.SOURCE_TOUCHSCREEN, 0);
        System.err.println("Event final " + event.toString());
        Log.i(TAG, "Event final for injectEvent" + event.toString());
        return event;
        //return injectEvent(event);
    }
    private static final float CalculatetheCoords(float a, float b, float alpha) {
        return (b - a) * alpha + a;
    }

    private void sendSwipe(float x1, float y1, float x2, float y2) {
        //System.err.println("sendSwipe Function called time is 15//11  : ");
        final int NUM_STEPS = 10;
        long now = SystemClock.uptimeMillis();
        injectPointerEvent(MotionEvent.obtain(now, now, MotionEvent.ACTION_DOWN, x1, y1, 0));
        for (int i = 1; i < NUM_STEPS; i++) {
            float alpha = (float)i / NUM_STEPS;
            injectPointerEvent(MotionEvent.obtain(now, now, MotionEvent.ACTION_MOVE, CalculatetheCoords(x1, x2, alpha), CalculatetheCoords(y1, y2, alpha), 0));
        }
        injectPointerEvent(MotionEvent.obtain(now, now, MotionEvent.ACTION_UP, x2, y2, 0));
    }

    private boolean injectMouseScroll(int xPoint, int yPoint, int hScroll, int vScroll) {
        long now = SystemClock.uptimeMillis();
        Point point = new Point(xPoint, yPoint);
        if (point == null) {
            return false;
        }
        MotionEvent.PointerProperties props = pointerProperties[0];
        props.id = 0;

        MotionEvent.PointerCoords coords = pointerCoords[0];
        coords.x = point.getX();
        coords.y = point.getY();
        coords.setAxisValue(MotionEvent.AXIS_HSCROLL, hScroll);
        coords.setAxisValue(MotionEvent.AXIS_VSCROLL, vScroll);


        System.err.println("Mouse Scroll X POINT "+ coords.x + " Y POINT "+coords.y);
        MotionEvent event = MotionEvent
                .obtain(lastTouchDown, now, MotionEvent.ACTION_SCROLL, 1, pointerProperties, pointerCoords, 0, 0, 1f, 1f, DEFAULT_DEVICE_ID, 0,
                        InputDevice.SOURCE_MOUSE, 0);
        System.err.println("Mouse Scroll Event "+ event.toString());
        return injectEvent(event);
    }

    public void type(String text) {
        KeyEvent[] events = keyCharacterMap.getEvents(text.toCharArray());

        if (events != null) {
            for (KeyEvent event : events) {
                final int metaState = event.getMetaState();
                final boolean controlDownFromEvent = event.isCtrlPressed();
                final boolean leftAltDownFromEvent = (metaState & KeyEvent.META_ALT_LEFT_ON) != 0;
                inputManager.injectKeyEvent(event);
            }
        }
    }


    private boolean injectTouch(int action, long pointerId, Position position, float pressure, int actionButton, int buttons) {
        long now = SystemClock.uptimeMillis();

        com.genymobile.scrcpy.Point point = device.getPhysicalPoint(position);
        if (point == null) {
            Ln.w("Ignore touch event, it was generated for a different device size");
            return false;
        }

        int pointerIndex = pointersState.getPointerIndex(pointerId);
        if (pointerIndex == -1) {
            Ln.w("Too many pointers for touch event");
            return false;
        }
        com.genymobile.scrcpy.Pointer pointer = pointersState.get(pointerIndex);
        pointer.setPoint(point);
        pointer.setPressure(pressure);

        int source;
        if (pointerId == POINTER_ID_MOUSE || pointerId == POINTER_ID_VIRTUAL_MOUSE) {
            // real mouse event (forced by the client when --forward-on-click)
            pointerProperties[pointerIndex].toolType = MotionEvent.TOOL_TYPE_MOUSE;
            source = InputDevice.SOURCE_MOUSE;
            pointer.setUp(buttons == 0);
        } else {
            // POINTER_ID_GENERIC_FINGER, POINTER_ID_VIRTUAL_FINGER or real touch from device
            pointerProperties[pointerIndex].toolType = MotionEvent.TOOL_TYPE_FINGER;
            source = InputDevice.SOURCE_TOUCHSCREEN;
            // Buttons must not be set for touch events
            buttons = 0;
            pointer.setUp(action == MotionEvent.ACTION_UP);
        }

        int pointerCount = pointersState.update(pointerProperties, pointerCoords);
        if (pointerCount == 1) {
            if (action == MotionEvent.ACTION_DOWN) {
                lastTouchDown = now;
            }
        } else {
            // secondary pointers must use ACTION_POINTER_* ORed with the pointerIndex
            if (action == MotionEvent.ACTION_UP) {
                action = MotionEvent.ACTION_POINTER_UP | (pointerIndex << MotionEvent.ACTION_POINTER_INDEX_SHIFT);
            } else if (action == MotionEvent.ACTION_DOWN) {
                action = MotionEvent.ACTION_POINTER_DOWN | (pointerIndex << MotionEvent.ACTION_POINTER_INDEX_SHIFT);
            }
        }

        /* If the input device is a mouse (on API >= 23):
         *   - the first button pressed must first generate ACTION_DOWN;
         *   - all button pressed (including the first one) must generate ACTION_BUTTON_PRESS;
         *   - all button released (including the last one) must generate ACTION_BUTTON_RELEASE;
         *   - the last button released must in addition generate ACTION_UP.
         *
         * Otherwise, Chrome does not work properly: <https://github.com/Genymobile/scrcpy/issues/3635>
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && source == InputDevice.SOURCE_MOUSE) {
            if (action == MotionEvent.ACTION_DOWN) {
                if (actionButton == buttons) {
                    // First button pressed: ACTION_DOWN
                    MotionEvent downEvent = MotionEvent.obtain(lastTouchDown, now, MotionEvent.ACTION_DOWN, pointerCount, pointerProperties,
                            pointerCoords, 0, buttons, 1f, 1f, DEFAULT_DEVICE_ID, 0, source, 0);
                    if (!device.injectEvent(downEvent, Device.INJECT_MODE_ASYNC)) {
                        return false;
                    }
                }

                // Any button pressed: ACTION_BUTTON_PRESS
                MotionEvent pressEvent = MotionEvent.obtain(lastTouchDown, now, MotionEvent.ACTION_BUTTON_PRESS, pointerCount, pointerProperties,
                        pointerCoords, 0, buttons, 1f, 1f, DEFAULT_DEVICE_ID, 0, source, 0);
                if (!com.genymobile.scrcpy.wrappers.InputManager.setActionButton(pressEvent, actionButton)) {
                    return false;
                }
                if (!device.injectEvent(pressEvent, Device.INJECT_MODE_ASYNC)) {
                    return false;
                }

                return true;
            }

            if (action == MotionEvent.ACTION_UP) {
                // Any button released: ACTION_BUTTON_RELEASE
                MotionEvent releaseEvent = MotionEvent.obtain(lastTouchDown, now, MotionEvent.ACTION_BUTTON_RELEASE, pointerCount, pointerProperties,
                        pointerCoords, 0, buttons, 1f, 1f, DEFAULT_DEVICE_ID, 0, source, 0);
                if (!com.genymobile.scrcpy.wrappers.InputManager.setActionButton(releaseEvent, actionButton)) {
                    return false;
                }
                if (!device.injectEvent(releaseEvent, Device.INJECT_MODE_ASYNC)) {
                    return false;
                }

                if (buttons == 0) {
                    // Last button released: ACTION_UP
                    MotionEvent upEvent = MotionEvent.obtain(lastTouchDown, now, MotionEvent.ACTION_UP, pointerCount, pointerProperties,
                            pointerCoords, 0, buttons, 1f, 1f, DEFAULT_DEVICE_ID, 0, source, 0);
                    if (!device.injectEvent(upEvent, Device.INJECT_MODE_ASYNC)) {
                        return false;
                    }
                }

                return true;
            }
        }

        MotionEvent event = MotionEvent
                .obtain(lastTouchDown, now, action, pointerCount, pointerProperties, pointerCoords, 0, buttons, 1f, 1f, DEFAULT_DEVICE_ID, 0, source,
                        0);
        return device.injectEvent(event, Device.INJECT_MODE_ASYNC);
    }

    private void injectPointerEvent(MotionEvent event) {
        event.setSource(InputDevice.SOURCE_TOUCHSCREEN);
        serviceManager.getInputManager().injectInputEvent(event, InputManager.INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH);
    }

    private boolean injectEvent(InputEvent event) {
        return serviceManager.getInputManager().injectInputEvent(event, InputManager.INJECT_INPUT_EVENT_MODE_ASYNC);
    }

    private void keyDown(int keyCode, int metaState) {
        long time = SystemClock.uptimeMillis();
        inputManager.injectKeyEvent(new KeyEvent(time, time, KeyEvent.ACTION_DOWN, keyCode, 0, metaState, deviceId, 0, KeyEvent.FLAG_FROM_SYSTEM, InputDevice.SOURCE_KEYBOARD));
    }

    private void keyUp(int keyCode, int metaState) {
        long time = SystemClock.uptimeMillis();
        inputManager.injectKeyEvent(new KeyEvent(
                time, time, KeyEvent.ACTION_UP, keyCode, 0, metaState, deviceId,0,                 KeyEvent.FLAG_FROM_SYSTEM,
                InputDevice.SOURCE_KEYBOARD
        ));
    }

    private boolean injectMouseHover(int xPoint, int yPoint) {
        long now = SystemClock.uptimeMillis();
        Point point = new Point(xPoint, yPoint);
        if (point == null) {
            return false;
        }

        MotionEvent.PointerProperties props = pointerProperties[0];
        props.id = 0;

        MotionEvent.PointerCoords coords = pointerCoords[0];
        coords.x = point.getX();
        coords.y = point.getY();

        System.err.println("Mouse Hover X POINT " + coords.x + " Y POINT " + coords.y);
        MotionEvent event = MotionEvent
                .obtain(now, now, MotionEvent.ACTION_HOVER_MOVE, 1, pointerProperties, pointerCoords, 0, 0, 1f, 1f, DEFAULT_DEVICE_ID, 0,
                        InputDevice.SOURCE_MOUSE, 0);

        System.err.println("Mouse Hover Event " + event.toString());
        return injectEvent(event);
    }

    private void keyDownReverseTab(int keyCode, int metaState) {
        long time = SystemClock.uptimeMillis();
        // Add the SHIFT meta state (KeyEvent.META_SHIFT_ON) to the existing meta state
        int shiftMetaState = metaState | KeyEvent.META_SHIFT_ON;

        inputManager.injectKeyEvent(new KeyEvent(time, time, KeyEvent.ACTION_DOWN,
                KeyEvent.KEYCODE_TAB, 0, shiftMetaState, deviceId, 0,
                KeyEvent.FLAG_FROM_SYSTEM, InputDevice.SOURCE_KEYBOARD));
    }

    private void keyUpReverseTab(int keyCode, int metaState) {
        long time = SystemClock.uptimeMillis();
        int shiftMetaState = metaState | KeyEvent.META_SHIFT_ON;

        inputManager.injectKeyEvent(new KeyEvent(time, time, KeyEvent.ACTION_UP,
                KeyEvent.KEYCODE_TAB, 0, shiftMetaState, deviceId, 0,
                KeyEvent.FLAG_FROM_SYSTEM, InputDevice.SOURCE_KEYBOARD));
    }
    @Override
    public void onDisconnected(String streamId) {
        Log.i(TAG, "onDisconnected: " + streamId);
    }

    @Override
    public void onPublishFinished(String streamId) {
        Log.i(TAG, "onPublishFinished: " + streamId);
    }

    @Override
    public void onPlayFinished(String streamId) {

    }

    @Override
    public void onPublishStarted(String streamId) {

        Log.i(TAG, "onPublishStarted: " + streamId);

    }

    @Override
    public void onPlayStarted(String streamId) {

    }

    @Override
    public void noStreamExistsToPlay(String streamId) {

    }

    @Override
    public void onError(String description, String streamId) {

    }

    @Override
    public void onSignalChannelClosed(WebSocket.WebSocketConnectionObserver.WebSocketCloseNotification code, String streamId) {

    }

    @Override
    public void streamIdInUse(String streamId) {

    }

    @Override
    public void onIceConnected(String streamId) {

    }

    @Override
    public void onIceDisconnected(String streamId) {

    }

    @Override
    public void onTrackList(String[] tracks) {

    }

    @Override
    public void onBitrateMeasurement(String streamId, int targetBitrate, int videoBitrate, int audioBitrate) {

    }

    @Override
    public void onStreamInfoList(String streamId, ArrayList<StreamInfo> streamInfoList) {

    }

    @Override
    public void onBufferedAmountChange(long previousAmount, String dataChannelLabel) {

    }

    @Override
    public void onStateChange(DataChannel.State state, String dataChannelLabel) {

    }

    @Override
    public void onMessage(DataChannel.Buffer buffer, String dataChannelLabel) {
        ByteBuffer data = buffer.data;
        final byte[] bytes = new byte[data.capacity()];
        data.get(bytes);
            String messageText = new String(bytes, StandardCharsets.UTF_8);
            Log.i(TAG, "Just Received from server " + messageText);
            int m_Rotation  =   device.getScreenInfo().getDeviceRotation();
            int m_Height    =   device.getScreenInfo().getVideoSize().getHeight();
            int m_Width     =   device.getScreenInfo().getVideoSize().getWidth();
            Log.i(TAG, "Device Current Rotation is  " + m_Rotation);
            Log.i(TAG, "Device Current Height   is  " + m_Height);
            Log.i(TAG, "Device Current Width    is  " + m_Width);
            if(messageText.indexOf('T', 0) == 0){
                Log.i(TAG, "TYPE_INJECT_TOUCH_EVENT ");
                   pointer.parseCoordinatesClick(messageText, m_Rotation,  m_Width, m_Height);//                injectTouch(pointer.getActionDown(), 1, pointer.getX(), pointer.getY());
                if (device.supportsInputEvents()) {
                    device.injectEvent(injectTouch(pointer.getActionDown(), 1, pointer.getX(), pointer.getY()), Device.INJECT_MODE_ASYNC);
                    device.injectEvent(injectTouch(pointer.getActionUP(), 1, pointer.getX(), pointer.getY()), Device.INJECT_MODE_ASYNC);
                }
            }else if(messageText.indexOf('S', 0) == 0 ) {
                Log.i(TAG, "TYPE_INJECT_TOUCH_SWIPE_EVENT ");
                pointer.parseCoordinatesSwipe(messageText, m_Rotation,  m_Width, m_Height);
                sendSwipe(pointer.getX(), pointer.getY(), pointer.getSwipeX(), pointer.getSwipeY());
            }else if(messageText.indexOf('H', 0) == 0 ) {
                Log.i(TAG, "TYPE_INJECT_MOUSE_HOVER_EVENT ");
                pointer.parseCoordinatesSwipe(messageText, m_Rotation,  m_Width, m_Height);
                injectMouseHover(pointer.getX(), pointer.getY());
            }else if(messageText.indexOf('n', 0) == 0){
                Log.i(TAG,"TYPE_INJECT_SCROLL_EVENT_DOWN");
                pointer.parseCoordinatesScrolling(messageText, m_Rotation,  m_Width, m_Height);
                injectMouseScroll(pointer.getX(), pointer.getY(), 0, 1);
            }else if(messageText.indexOf('N', 0) == 0){
                Log.i(TAG,"TYPE_INJECT_SCROLL_EVENT_UP: " + messageText);
                pointer.parseCoordinatesScrolling(messageText, m_Rotation,  m_Width, m_Height);
                injectMouseScroll(pointer.getX(), pointer.getY(), 0, -1);
            }else if(messageText.indexOf('t', 0) == 0){
                pointer.parseCoordinatesClick(messageText, m_Rotation,  m_Width, m_Height);
                Log.i(TAG,"TYPE_INJECT_LONG_TOUCH_EVENT : " + messageText);
                long endTime = SystemClock.uptimeMillis() + 700;
                if (device.supportsInputEvents()) {
                    device.injectEvent(injectTouch(pointer.getActionDown(), 1, pointer.getX(), pointer.getY()), Device.INJECT_MODE_ASYNC);
                    while (SystemClock.uptimeMillis() < endTime) {
                    //loop
                }
                    device.injectEvent(injectTouch(pointer.getActionUP(), 1, pointer.getX(), pointer.getY()), Device.INJECT_MODE_ASYNC);
                }
            }else if(messageText.indexOf('K', 0) == 0){

                if(pointer.parseKey(messageText)){
                    Log.i(TAG,"TYPE_INJECT_KEYCODE : " + messageText);
                    if (pointer.getKeyCode() == 59 && messageText.contains("shift")) {
                        int shiftKeyCode;
                        Log.i(TAG,"TYPE_INJECT_KEYCODE shift: " + messageText);
                        if (messageText.contains("right_shift")) {
                            shiftKeyCode = KeyEvent.KEYCODE_SHIFT_RIGHT;
                        } else {
                            shiftKeyCode = KeyEvent.KEYCODE_SHIFT_LEFT;
                        }
                        int shiftMetaState;
                        if (shiftKeyCode == KeyEvent.KEYCODE_SHIFT_RIGHT) {
                            shiftMetaState = KeyEvent.META_SHIFT_ON | KeyEvent.META_SHIFT_RIGHT_ON;
                        } else {
                            shiftMetaState = KeyEvent.META_SHIFT_ON | KeyEvent.META_SHIFT_LEFT_ON;
                        }
                        keyDown(shiftKeyCode, 0);
                        keyDown(KeyEvent.KEYCODE_TAB, shiftMetaState);
                        keyUp(KeyEvent.KEYCODE_TAB, shiftMetaState);
                        keyUp(shiftKeyCode, 0);
                        Log.i(TAG,"TYPE_INJECT_KEYCODE 2: " + pointer.getKeyCode()+" "+KeyEvent.KEYCODE_TAB+" "+shiftKeyCode);
                    } else {
                        keyDown(pointer.getKeyCode(), 0);
                        keyUp(pointer.getKeyCode(), 0);
                        Log.i(TAG,"TYPE_INJECT_KEYCODE 1: " + pointer.getKeyCode());
                    }
                }else {
                    Log.i(TAG,"TYPE_INJECT_TEXT : " + messageText);
                    type(messageText.substring(1).trim());
                }
            }else{
                Log.i(TAG,"Unknown meta data : " + messageText);
            }
    }

    @Override
    public void onMessageSent(DataChannel.Buffer buffer, boolean successful) {

    }
}
