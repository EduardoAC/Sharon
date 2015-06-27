package sharon.sharon;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;


/**
 * Created by Eduardo.aparicio.cardenes@gmail.com on 27/06/2015.
 */
public class VodView extends VideoView {

    public VodView(Context context) {
        super(context);
    }

    public VodView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VodView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    /**
     * Resize video view by using SurfaceHolder.setFixedSize(...). See {@link android.view.SurfaceHolder#setFixedSize}
     * @param width
     * @param height
     */
    public void setFixedVideoSize(int width, int height)
    {
        getHolder().setFixedSize(width, height);
    }
}