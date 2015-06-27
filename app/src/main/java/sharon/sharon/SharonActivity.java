package sharon.sharon;

import android.net.Uri;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sharon);
        mRootParam = (LayoutParams) ((View) findViewById(R.id.root_view)).getLayoutParams();
        mVodView = (VodView) findViewById(R.id.vodView1);

        // Video Uri
        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + +R.raw.longvideo);
        mVodView.setVideoURI(uri);
        mVodView.requestFocus();
        mVodView.start();
        // set up gesture listeners
        mScaleGestureDetector = new ScaleGestureDetector(this, new MyScaleGestureListener());
        mGestureDetector = new GestureDetector(this, new MySimpleOnGestureListener());

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
    private class MySimpleOnGestureListener extends SimpleOnGestureListener {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (mVodView == null)
                return false;
            if (mVodView.isPlaying())
                mVodView.pause();
            else
                mVodView.start();
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
