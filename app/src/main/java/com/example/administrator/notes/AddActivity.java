package com.example.administrator.notes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class AddActivity extends Activity {
	private Button bt_back;
	private Button bt_save;
	private TextView tv_title;
	private SQLiteDatabase db;
	private DatabaseOperation dop;
	
	private EditText et_Notes;
	private GridView bottomMenu;
	private int[] bottomItems = {
			R.drawable.tabbar_camera,
	};
	InputMethodManager imm;
	Intent intent;
	String editModel = null;
	int item_Id;
	
	private static String IMGPATH = "/sdcard/notes/yyyyMMddHHmmsspaint.png";
	private List<Map<String,String>> imgList = new ArrayList<Map<String,String>>();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_add);
		//getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_add);

		//LayoutInflater factorys=LayoutInflater.from(AddActivity.this);
		//final View v=factorys.inflate(R.layout.title_add,null);
		bt_back = (Button)findViewById(R.id.bt_back);
		bt_back.setOnClickListener(new ClickEvent());
		bt_save = (Button)findViewById(R.id.bt_save);
		bt_save.setOnClickListener(new ClickEvent());
		tv_title = (TextView)findViewById(R.id.tv_title);
		et_Notes = (EditText)findViewById(R.id.et_note);
		
		bottomMenu = (GridView)findViewById(R.id.bottomMenu);
		

		initBottomMenu();
		bottomMenu.setOnItemClickListener(new MenuClickEvent());
		//et_Notes.setFocusable(false);
		
		imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
	    imm.hideSoftInputFromWindow(et_Notes.getWindowToken(),0);
		dop = new DatabaseOperation(this,db);
		intent = getIntent();
		editModel = intent.getStringExtra("editModel");
		item_Id = intent.getIntExtra("noteId", 0);
		loadData();
		et_Notes.setOnClickListener(new TextClickEvent());
	}
	private void loadData(){

		if(editModel.equals("newAdd")){
			et_Notes.setText("");
		}
		else if(editModel.equals("update")){
			tv_title.setText("write");
			
			dop.create_db();
			Cursor cursor = dop.query_db(item_Id);
			cursor.moveToFirst();
			String context = cursor.getString(cursor.getColumnIndex("context"));

		    Pattern p=Pattern.compile("/([^\\.]*)\\.\\w{3}"); 
		    Matcher m=p.matcher(context);
		    int startIndex = 0;
		    while(m.find()){
		    	if(m.start() > 0){
		    		et_Notes.append(context.substring(startIndex, m.start()));
		    	}
		    	
		        SpannableString ss = new SpannableString(m.group().toString());
		    	String path = m.group().toString();
		    	String type = path.substring(path.length() - 3, path.length());
		    	Bitmap bm = null;
		    	Bitmap rbm = null;
		    	if(type.equals("amr")){
		    		bm = BitmapFactory.decodeResource(getResources(), R.drawable.record_icon);
			        rbm = resize(bm,200);
		    	}
		    	else{
			        bm = BitmapFactory.decodeFile(m.group());
			        rbm = resize(bm,480);
		    	}

		        rbm = getBitmapHuaSeBianKuang(rbm);
		       
		        ImageSpan span = new ImageSpan(this, rbm);
		        ss.setSpan(span,0, m.end() - m.start(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		        System.out.println(m.start()+"-------"+m.end());
		        et_Notes.append(ss);
		        startIndex = m.end();

		        Map<String,String> map = new HashMap<String,String>();
		        map.put("location", m.start()+"-"+m.end());
		        map.put("path", path);
		        imgList.add(map);
		    }
		    et_Notes.append(context.substring(startIndex,context.length()));
			dop.close_db();
		}	
	}
	class TextClickEvent implements OnClickListener{

		@Override
		public void onClick(View v) {
			Spanned s = et_Notes.getText();
			ImageSpan[] imageSpans;
			imageSpans = s.getSpans(0, s.length(), ImageSpan.class);
			
			int selectionStart = et_Notes.getSelectionStart();
			for(ImageSpan span : imageSpans){
				
				int start = s.getSpanStart(span);
				int end = s.getSpanEnd(span);
				if(selectionStart >= start && selectionStart < end){
					String path = null;
					for(int i = 0;i < imgList.size();i++){
						Map map = imgList.get(i);
						if(map.get("location").equals(start+"-"+end)){
							path = imgList.get(i).get("path");
							break;
						}
					}
					Intent intent = new Intent(AddActivity.this,ShowPicture.class);
					intent.putExtra("imgPath", path);
					startActivity(intent);
				}
				else
					imm.showSoftInput(et_Notes, 0);
			}
		}
	}
	class TextTouchEvent implements OnTouchListener{

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			
			Spanned s = et_Notes.getText();
			ImageSpan[] imageSpans;
			imageSpans = s.getSpans(0, s.length(), ImageSpan.class);
			
			int selectionStart = et_Notes.getSelectionStart();
			for(ImageSpan span : imageSpans){
				
				int start = s.getSpanStart(span);
				int end = s.getSpanEnd(span);
				int inType = et_Notes.getInputType();
				if(selectionStart >= start && selectionStart < end){
					Bitmap bitmap = ((BitmapDrawable)span.getDrawable()).getBitmap();
					System.out.println(span.getSource()+"---------------------------");
					 
	                et_Notes.setInputType(InputType.TYPE_NULL); // disable soft input 
	                et_Notes.onTouchEvent(event); // call native handler      
	                et_Notes.setInputType(inType); // restore input type
					AddActivity.this.finish();
					
				}
				else{
					imm.showSoftInput(et_Notes, 0);
					et_Notes.setInputType(inType);
				}
			}	
			return true;
			
		}
		
	}
	

	class ClickEvent implements OnClickListener{

		@Override
		public void onClick(View v) {
			switch(v.getId()){
			case R.id.bt_back :
				AddActivity.this.finish();
				break;

			case R.id.bt_save :
				String context = et_Notes.getText().toString();
				if(context.isEmpty()){
					Toast.makeText(AddActivity.this, "empty!", Toast.LENGTH_LONG).show();
				}
				else{
					SimpleDateFormat   formatter   =   new   SimpleDateFormat   ("yyyy-MM-dd HH:mm");  
		            Date   curDate   =   new   Date(System.currentTimeMillis());
		            String   time   =   formatter.format(curDate);
		            String title = getTitle(context);
					dop.create_db();
					if(editModel.equals("newAdd")){
						dop.insert_db(title,context,time);
					}
					else if(editModel.equals("update")){
						dop.update_db(title,context,time,item_Id);
					}
					dop.close_db();
					AddActivity.this.finish();
				}
				break;
			}	
		}
	}
	private String getTitle(String context){
	    Pattern p=Pattern.compile("/([^\\.]*)\\.\\w{3}");
	    Matcher m=p.matcher(context);
	    StringBuffer strBuff = new StringBuffer();
	    String title = "";
	    int startIndex = 0;
	    while(m.find()){
	    	if(m.start() > 0){
	    		strBuff.append(context.substring(startIndex, m.start()));
	    	}
	    	String path = m.group().toString();
	    	String type = path.substring(path.length() - 3, path.length());
	    	if(type.equals("amr")){
	    		strBuff.append("[sound]");
	    	}
	    	else{
	    		strBuff.append("[pic]");
	    	}
	        startIndex = m.end();
	        if(strBuff.length() > 15){
	        	title = strBuff.toString().replaceAll("\r|\n|\t", " ");
	        	return title;
	        }
	    }
	    strBuff.append(context.substring(startIndex, context.length()));
	    title = strBuff.toString().replaceAll("\r|\n|\t", " ");
        return title;
	}
	

	private void initBottomMenu(){
		ArrayList<Map<String,Object>> menus = new ArrayList<Map<String,Object>>();
		for(int i = 0;i < bottomItems.length;i++){
			Map<String,Object> item = new HashMap<String,Object>();
			item.put("image",bottomItems[i]);
			menus.add(item);
		}
		bottomMenu.setNumColumns(bottomItems.length);
		bottomMenu.setSelector(R.drawable.bottom_item);
		SimpleAdapter mAdapter = new SimpleAdapter(AddActivity.this, menus,R.layout.item_button, new String[]{"image"}, new int[]{R.id.item_image});
		bottomMenu.setAdapter(mAdapter);
	}

	class MenuClickEvent implements OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			Intent intent;
			switch(position){
			case 0:
				intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
				startActivityForResult(intent, 2);
				break;
				
			}
			
		}
		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK){
			Uri uri = data.getData();
			ContentResolver cr = AddActivity.this.getContentResolver();
			Bitmap bitmap=null;
			Bundle extras = null;
			if(requestCode == 1){
				
				String[] proj = { MediaStore.Images.Media.DATA };   
	            Cursor actualimagecursor = managedQuery(uri,proj,null,null,null);   
				int actual_image_column_index = actualimagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);   
				actualimagecursor.moveToFirst();
	            String path = actualimagecursor.getString(actual_image_column_index);  
				try {
					bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));
					
					
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				InsertBitmap(bitmap,480,path);
			}
			else if(requestCode == 2){
				
				try {
					
					if(uri != null)
						bitmap = MediaStore.Images.Media.getBitmap(cr, uri);
					else{
						extras = data.getExtras();
						bitmap = extras.getParcelable("data");
					}
		      		SimpleDateFormat   formatter   =   new   SimpleDateFormat   ("yyyyMMddHHmmss");  
		            Date   curDate   =   new   Date(System.currentTimeMillis());
		            String   str   =   formatter.format(curDate);  
		            String paintPath = "";
		            str = str + "paint.png";
		            File dir = new File("/sdcard/notes/");
		            File file = new File("/sdcard/notes/",str);
		            if (!dir.exists()) { 
		            	dir.mkdir(); 
		            } 
		            else{
		            	if(file.exists()){
		            		file.delete();
		            	}
		            }
					FileOutputStream fos = new FileOutputStream(file);
					bitmap.compress(CompressFormat.PNG, 100, fos);
					fos.flush();
					fos.close();
					String path = "/sdcard/notes/" + str;
					InsertBitmap(bitmap,480,path);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	void InsertBitmap(Bitmap bitmap,int S,String imgPath){
		
		bitmap = resize(bitmap,S);
		bitmap = getBitmapHuaSeBianKuang(bitmap);
		//bitmap = addBigFrame(bitmap,R.drawable.line_age);
		final ImageSpan imageSpan = new ImageSpan(this,bitmap);
		SpannableString spannableString = new SpannableString(imgPath);
		spannableString.setSpan(imageSpan, 0, spannableString.length(), SpannableString.SPAN_MARK_MARK);
		//et_Notes.append("\n");
		Editable editable = et_Notes.getEditableText();
		int selectionIndex = et_Notes.getSelectionStart();
		spannableString.getSpans(0, spannableString.length(), ImageSpan.class);

		editable.insert(selectionIndex, spannableString);
		et_Notes.append("\n");

        Map<String,String> map = new HashMap<String,String>();
        map.put("location", selectionIndex+"-"+(selectionIndex+spannableString.length()));
        map.put("path", imgPath);
        imgList.add(map);
	}
	private Bitmap resize(Bitmap bitmap,int S){
		int imgWidth = bitmap.getWidth();
		int imgHeight = bitmap.getHeight();
		double partion = imgWidth*1.0/imgHeight;
		double sqrtLength = Math.sqrt(partion*partion + 1);
		double newImgW = S*(partion / sqrtLength);
		double newImgH = S*(1 / sqrtLength);
		float scaleW = (float) (newImgW/imgWidth);
		float scaleH = (float) (newImgH/imgHeight);
		
		Matrix mx = new Matrix();
		mx.postScale(scaleW, scaleH);
		bitmap = Bitmap.createBitmap(bitmap, 0, 0, imgWidth, imgHeight, mx, true);
		return bitmap;
	}
	public Bitmap getBitmapHuaSeBianKuang(Bitmap bitmap) {
        float frameSize = 0.2f;
        Matrix matrix = new Matrix();

        Bitmap bitmapbg = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmapbg);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
                | Paint.FILTER_BITMAP_FLAG));
 
        float scale_x = (bitmap.getWidth() - 2 * frameSize - 2) * 1f
                / (bitmap.getWidth());
        float scale_y = (bitmap.getHeight() - 2 * frameSize - 2) * 1f
                / (bitmap.getHeight());
        matrix.reset();
        matrix.postScale(scale_x, scale_y);

        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), matrix, true);
 
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(1);
        paint.setStyle(Style.FILL);

        canvas.drawRect(
                new Rect(0, 0, bitmapbg.getWidth(), bitmapbg.getHeight()),
                paint);
        paint.setColor(Color.BLUE);
        canvas.drawRect(
                new Rect((int) (frameSize), (int) (frameSize), bitmapbg
                        .getWidth() - (int) (frameSize), bitmapbg.getHeight()
                        - (int) (frameSize)), paint);
 
        canvas.drawBitmap(bitmap, frameSize + 1, frameSize + 1, paint);
 
        return bitmapbg;
	}
}

