package com.example.administrator.notes;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;

public class ShowPicture extends Activity {
	private ImageView img;
	private Bitmap bm;
	private DisplayMetrics dm;  
	private Matrix matrix = new Matrix();
	private Matrix savedMatrix = new Matrix();
	private PointF mid = new PointF();
	private PointF start = new PointF();
	private static int DRAG = 2;
	private static int ZOOM = 1;
	private static int NONE = 0;
	private int mode = 0;
	private float oldDist = 1f;
	private static float MINSCALER = 0.3f;
	private static float MAXSCALER = 3.0f;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_show_picture);
		//getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_add);
		TextView tv_title = (TextView)findViewById(R.id.tv_title);
		tv_title.setText("picture");
		Button bt_back = (Button)findViewById(R.id.bt_back);
		bt_back.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				ShowPicture.this.finish();
			}
		});
		Button bt_del = (Button)findViewById(R.id.bt_save);
		bt_del.setBackgroundResource(R.drawable.paint_icon_delete);
		
		dm = new DisplayMetrics();  
        getWindowManager().getDefaultDisplay().getMetrics(dm);
		
		
		
		img = (ImageView)findViewById(R.id.iv_showPic);
		
		Intent intent = this.getIntent();
		String imgPath = intent.getStringExtra("imgPath");
		bm = BitmapFactory.decodeFile(imgPath);
		savedMatrix.setTranslate((dm.widthPixels - bm.getWidth())/2 , (dm.heightPixels - bm.getHeight()) / 2);
		img.setImageMatrix(savedMatrix);
		img.setImageBitmap(bm);
		img.setScaleType(ScaleType.MATRIX);
		img.setOnTouchListener(new TouchEvent());
	}

	class TouchEvent implements OnTouchListener{
		@Override
		public boolean onTouch(View view, MotionEvent event) {
			switch(event.getActionMasked()){
			   	case MotionEvent.ACTION_DOWN :
			   		matrix.set(img.getImageMatrix());
			   		savedMatrix.set(matrix);
			   		start.set(event.getX(), event.getY());
			   		mode = DRAG;
			   		break;
				case MotionEvent.ACTION_POINTER_DOWN :
					oldDist = getSpacing(event);
					savedMatrix.set(matrix);
					getMidPoint(mid,event);
					mode = ZOOM;
					break;
				case MotionEvent.ACTION_POINTER_UP :
					mode = NONE;
					break;
				case MotionEvent.ACTION_MOVE :
					if(mode == DRAG){
						matrix.set(savedMatrix);
						matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);
					}
					else if(mode == ZOOM){
						float newDist = getSpacing(event);
						if(newDist > 10){
							matrix.set(savedMatrix);
							float scale = newDist / oldDist;
							
							matrix.postScale(scale, scale,mid.x,mid.y);
						}
					}
					break;
			}
			img.setImageMatrix(matrix);
			controlScale();
			//setCenter();
			center();
			return true;
		}
	}

	private float getSpacing(MotionEvent event){
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return (float)Math.sqrt(x * x + y * y);
	}

	private void getMidPoint(PointF mid,MotionEvent event){
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		mid.set(x / 2, y / 2);
	}
	private void controlScale(){
		float values[] = new float[9];
		matrix.getValues(values);
		if(mode == ZOOM){
			if(values[0] < MINSCALER)
				matrix.setScale(MINSCALER, MINSCALER);
			else if(values[0] > MAXSCALER)
				matrix.setScale(MAXSCALER, MAXSCALER);
		}
	}
    protected void center()  
    {  
        center(true,true);  
    }  
  
    private void center(boolean horizontal, boolean vertical)  
    {  
        Matrix m = new Matrix();  
        m.set(matrix);  
        RectF rect = new RectF(0, 0, bm.getWidth(), bm.getHeight());  
        m.mapRect(rect);  
        float height = rect.height();  
        float width = rect.width();  
        float deltaX = 0, deltaY = 0;  
        if (vertical)  
        {  
            int screenHeight = dm.heightPixels;
            //int screenHeight = 400;  
            if (height < screenHeight)  
            {  
                deltaY = (screenHeight - height)/2 - rect.top;  
            }else if (rect.top > 0)  
            {  
                deltaY = -rect.top;  
            }else if (rect.bottom < screenHeight)  
            {  
                deltaY = screenHeight - rect.bottom;  
            }  
        }  
          
        if (horizontal)  
        {  
            int screenWidth = dm.widthPixels;
            //int screenWidth = 400;  
            if (width < screenWidth)  
            {  
                deltaX = (screenWidth - width)/2 - rect.left;  
            }else if (rect.left > 0)  
            {  
                deltaX = -rect.left;      
            }else if (rect.right < screenWidth)  
            {  
                deltaX = screenWidth - rect.right;  
            }  
        }  
        matrix.postTranslate(deltaX, deltaY);  
    }  
}
