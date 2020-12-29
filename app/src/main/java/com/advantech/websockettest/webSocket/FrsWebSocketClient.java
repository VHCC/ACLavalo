package com.advantech.websockettest.webSocket;

import android.media.FaceDetector;

import com.advantech.websockettest.util.MLog;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Map;

import androidx.annotation.NonNull;


/**
 * Created by IChen.Chu on 2020/5/14
 */
public class FrsWebSocketClient extends WebSocketClient {

    private static final MLog mLog = new MLog(true);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());


    public FrsWebSocketClient(URI serverUri) {
        super(serverUri);
    }

    public FrsWebSocketClient(URI serverUri, Draft protocolDraft) {
        super(serverUri, protocolDraft);
    }

    public FrsWebSocketClient(URI serverUri, Map<String, String> httpHeaders) {
        super(serverUri, httpHeaders);
    }

    public FrsWebSocketClient(URI serverUri, Draft protocolDraft, Map<String, String> httpHeaders) {
        super(serverUri, protocolDraft, httpHeaders);
    }

    public FrsWebSocketClient(URI serverUri, Draft protocolDraft, Map<String, String> httpHeaders, int connectTimeout) {
        super(serverUri, protocolDraft, httpHeaders, connectTimeout);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        mLog.i(TAG, " + opened connection, " + handshakedata.getHttpStatusMessage());
//        isFRServerConnected = true;
    }

    public static JSONObject staticPersonRecognitionResult;
    public static JSONObject staticPersonInfo;
    public static String staticPersonID;

    @Override
    public void onMessage(String message) {
        mLog.i(TAG, message);

        try {
            JSONObject tempResult = new JSONObject(message);

            int type = tempResult.getInt("type");
            String channel = tempResult.getString("channel");


//            mLog.i(TAG, " *** type: " + type );
//            mLog.i(TAG, " *** channel: " + channel );
//            if (VFREdgeCache.getInstance().getTabletID().equals(channel)) {
//                mLog.i(TAG, " *** type: " + type );
//                mLog.i(TAG, " *** channel: " + channel );
////                mLog.d(TAG, "received: " + message );
//                staticPersonRecognitionResult = tempResult;
//
//                switch (type) {
//                    case 0: // unRecognized
//                        if (staticVerifySwitch) {
//                            AppBus.getInstance().post(new BusEvent("verify unRecognized", APP_CODE_FRS_VERIFY_UN_RECOGNIZED));
//                        }
//                        staticVerifySwitch = false;
////                    AppBus.getInstance().post(new BusEvent("face detect done", 2001));
//                        break;
//                    case 1: // success
//                        JSONObject person_info = tempResult.getJSONObject("person_info");
//                        mLog.i(TAG, " *** person_info: " + person_info.toString() );
//                        staticPersonID = staticPersonRecognitionResult.getString("person_id");
//                        if (staticVerifySwitch) {
//                            staticPersonInfo = staticPersonRecognitionResult.getJSONObject("person_info");
//                            AppBus.getInstance().post(new BusEvent("verify successfully", APP_CODE_FRS_VERIFY_SUCCESS));
//                        }
//                        staticVerifySwitch = false;
////                    AppBus.getInstance().post(new BusEvent("face detect done", 2001));
//                        break;
//                }
//            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setListener (frsListener listener) {
        frsListener = listener;
    }

    private frsListener frsListener;

    public interface frsListener {

//        ByteBuffer onMessage(RGB[] rgbs);
        void onMessage(byte[] bytesResult);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        mLog.i(TAG, " + Connection closed by " + (remote ? "remote peer" : "us") + ", Code: " + code + ", Reason: " + reason);
//        isFRServerConnected = false;
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
        mLog.i(TAG, " + error= " + ex);
    }

    @Override
    public void onMessage(ByteBuffer bytes) {
//        mLog.i(TAG, "limit= " + bytes.limit());
//        mLog.i(TAG, "remaining= " + bytes.remaining());
//        mLog.i(TAG, "position= " + bytes.position());

        byte[] bytes1 = new byte[4];
        byte[] bytesResult = new byte[4*768];
        RGB[] tmps = new RGB[768];


        for (int index = 0; index < 768; index++) {
            int x = 0;
            for (x = 0; x < 4; x++) {
//            mLog.i(TAG, bytes.get(x) + "");
                bytes1[x] = bytes.get(4 * index + x);
            }
            float tmp = ByteBuffer.wrap(bytes1).order(ByteOrder.LITTLE_ENDIAN).getFloat();
            tmps[index] = rgbGet(tmp);
            mLog.d(TAG, "tmp= " + tmp + ", " +tmps[index].toString());
            int qqq = 4 * index;
//            if (qqq > 3000) {
//                mLog.d(TAG, qqq + "");
//                mLog.d(TAG, "index= " + index);
//                mLog.d(TAG, "x= " + x);
//            }
            bytesResult[4 * index + 0] = (byte)tmps[index].r;
            bytesResult[4 * index + 1] = (byte)tmps[index].g;
            bytesResult[4 * index + 2] = (byte)tmps[index].b;
            bytesResult[4 * index + 3] = (byte)tmps[index].max;
        }


        mLog.d(TAG, "done");
        frsListener.onMessage(bytesResult);

    }

    private double rgb_min = 23.30737d;
    private double rgb_max = 34.0d;

    private RGB rgbGet(double tmp) {
        double rate = 1.0d - (tmp - rgb_min) / (rgb_max - rgb_min);
        if (rate < 0) {
            rate = 0;
        } else if (rate > 1) {
            rate = 1;
        }
//        mLog.d(TAG, "rate:> " + rate);
        double h = (Math.tanh(rate * 2 - 1.5) + 1) / 2 - 0.04;
//        mLog.d(TAG, "h:> " + h);
        return HSVtoRGB(h, 1, 1);
    }

    public class RGB {
        int r = 0;
        int g = 0;
        int b = 0;
        int max = 255;

        @NonNull
        @Override
        public String toString() {
            return "RGB:> r= " + r + ", g= " + g + ", b= " + b;
        }
    }

    private RGB HSVtoRGB(double h, int s, int v) {
        double r = 0d;
        double g = 0d;
        double b = 0d;
        double i, f, p, q, t;
        i = Math.floor(h * 6);
        f = h * 6 - i;
        p = v * (1 - s);
        q = v * (1 - f * s);
        t = v * (1 - (1 - f) * s);

//        mLog.d(TAG, "f= " + f);
//        mLog.d(TAG, "p= " + p);
//        mLog.d(TAG, "q= " + q);
//        mLog.d(TAG, "t= " + t);

        RGB rgb = new RGB();

//        mLog.d(TAG, "(int)i % 6:> " + (int)i % 6);
        switch ((int) i % 6) {
            case 0:
                r = v;
                g = t;
                b = p;
                break;
            case 1:
                r = q;
                g = v;
                b = p;
                break;
            case 2:
                r = p;
                g = v;
                b = t;
                break;
            case 3:
                r = p;
                g = q;
                b = v;
                break;
            case 4:
                r = t;
                g = p;
                b = v;
                break;
            case 5:
                r = v;
                g = p;
                b = q;
                break;
        }
        rgb.r = (int) Math.round(r * 255d);
        rgb.g = (int) Math.round(g * 255d);
        rgb.b = (int) Math.round(b * 255d);

        return rgb;
    }

}
