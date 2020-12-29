package com.advantech.websockettest;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;

import com.advantech.websockettest.util.MLog;
import com.advantech.websockettest.webSocket.FrsWebSocketClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {

    private static final MLog mLog = new MLog(true);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

    // field
    private ImageView imageView;

    FrsWebSocketClient c;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.image);
        connectFRSServer ();
    }

    private void connectFRSServer () {
        mLog.d(TAG, " * connect Themo Server");
        if (c != null) {
            c.close();
        }
        c = null; // more about drafts here: http://github.com/TooTallNate/Java-WebSocket/wiki/Drafts
        try {
//            c = new FrsWebSocketClient( new URI( "ws://" +  "192.168.4.1:9999"  ));
            c = new FrsWebSocketClient( new URI("ws://192.168.4.1:9999"));
            c.setListener(new FrsWebSocketClient.frsListener() {
                @Override
                public void onMessage(byte[] bytesResult) {
                    mLog.d(TAG, "!!!!!");
                    Bitmap bitmap = Bitmap.createBitmap(32, 24, Bitmap.Config.ARGB_8888);
                    ByteBuffer buffer = ByteBuffer.wrap(bytesResult);

                    bitmap.copyPixelsFromBuffer(buffer);
//                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, 32, 24);
//                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytesResult, 0, bytesResult.length);


                    Message msg = new Message();
                    msg.obj = bitmap;
                    mHandler.sendMessage(msg);
                    return;
                }

            });
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        c.connect();
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            imageView.setImageBitmap((Bitmap) msg.obj);
        }
    };

}