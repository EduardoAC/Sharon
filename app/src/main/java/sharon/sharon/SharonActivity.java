package sharon.sharon;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.TextView;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

//import com.android.volley.RequestQueue;
//import com.android.volley.toolbox.Volley;

import java.net.URI;
import java.net.URISyntaxException;

public class SharonActivity extends ActionBarActivity {
    // minimum video view width
    static final int MIN_WIDTH = 100;
    // Root view's LayoutParams
    private FrameLayout.LayoutParams mRootParam;
    // Custom Video View
    private VodView mVodView;
    // detector to pinch zoom in/out
    private ScaleGestureDetector mScaleGestureDetector;
    // detector to single tab
    private GestureDetector mGestureDetector;

    //private RequestQueue queue;
    private String serverUrl = "/http://85.214.151.40/";
    private WebSocketClient mWebSocketClient;
    private boolean enabeSendMessages = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sharon);
        mRootParam = (LayoutParams) ((View) findViewById(R.id.root_view)).getLayoutParams();
        mVodView = (VodView) findViewById(R.id.vodView1);

        // Video Uri
        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + +R.raw.canon_gladiator);
        //Uri uri = Uri.parse("http://85.214.151.40/canon_gladiator.mp4");

        mVodView.setVideoURI(uri);
        mVodView.requestFocus();
        mVodView.start();
        // set up gesture listeners
        mScaleGestureDetector = new ScaleGestureDetector(this, new MyScaleGestureListener());
        mGestureDetector = new GestureDetector(this, new MySimpleOnGestureListener());

        connectWebSocket();
        //queue = Volley.newRequestQueue(this);

        mVodView.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mGestureDetector.onTouchEvent(event);
                mScaleGestureDetector.onTouchEvent(event);
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sharon, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onResume() {
        mVodView.start();
        super.onResume();
    }

    @Override
    protected void onPause() {
        mVodView.pause();
        super.onPause();
    }

    private void connectWebSocket() {
        URI uri;
        try {
            uri = new URI("ws://85.214.151.40:9000/sharonserver/server.php");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        mWebSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.i("Websocket", "Opened");
                mWebSocketClient.send("{\"name\":\"android\",\"message\":" +"\"Hello from " + Build.MANUFACTURER + " " + Build.MODEL+ "\",\"color\":\"000000\"}");
            }

            @Override
            public void onMessage(String s) {
                final String message = s;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject jObject = new JSONObject(message);
                            String code = jObject.getString("message");
                            Log.i("Websocket.receive", message);
                            Log.i("Websocket.code", code);
                            if(code == "0"){
                                return;
                            }
                            if(code.equals("1")){
                                if(mVodView.isPlaying()){
                                    mVodView.pause();
                                }
                            }else if(code.equals("2")){
                                if(!mVodView.isPlaying()){
                                    mVodView.start();
                                    enabeSendMessages = true;
                                }
                            }else if(code.equals("3")){

                            }else if(code.equals("4")){

                            }else{

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                Log.i("Websocket", "Closed " + s);
            }

            @Override
            public void onError(Exception e) {
                Log.i("Websocket", "Error " + e.getMessage());
            }
        };
        mWebSocketClient.connect();
    }

    public void sendMessage(String message) {
//        if(enabeSendMessages){
            mWebSocketClient.send("{\"name\":\"android\",\"message\":\"" +message+ "\",\"color\":\"000000\"}");
//        }
        Log.d("onMessage", "Message=" + message);
    }

    private class MySimpleOnGestureListener extends SimpleOnGestureListener {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (mVodView == null)
                return false;
            if (mVodView.isPlaying()) {
                mVodView.pause();
                sendMessage("1");
            }else {
                mVodView.start();
                sendMessage("2");
            }
            return true;
        }

    }

    private class MyScaleGestureListener implements OnScaleGestureListener {
        private int mW, mH;

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            // scale our video view
            mW *= detector.getScaleFactor();
            mH *= detector.getScaleFactor();
            if (mW < MIN_WIDTH) { // limits width
                mW = mVodView.getWidth();
                mH = mVodView.getHeight();
            }
            Log.d("onScale", "scale=" + detector.getScaleFactor() + ", w=" + mW + ", h=" + mH);
            mVodView.setFixedVideoSize(mW, mH); // important
            sendMessage(mW+","+mH);
            mRootParam.width = mW;
            mRootParam.height = mH;
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            mW = mVodView.getWidth();
            mH = mVodView.getHeight();
            Log.d("onScaleBegin", "scale=" + detector.getScaleFactor() + ", w=" + mW + ", h=" + mH);
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            Log.d("onScaleEnd", "scale=" + detector.getScaleFactor() + ", w=" + mW + ", h=" + mH);
        }

    }
}
