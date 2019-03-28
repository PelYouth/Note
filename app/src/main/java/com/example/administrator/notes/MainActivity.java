package com.example.administrator.notes;



import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;


public class MainActivity extends Activity {
	private Button bt_add;
	private SQLiteDatabase db;
	private DatabaseOperation dop;
	private ListView lv_notes;
	private TextView tv_note_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_main);
        //getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_main);




        //LayoutInflater factorys=LayoutInflater.from(MainActivity.this);
		//final View v=factorys.inflate(R.layout.title_main,null);
        bt_add =(Button)findViewById(R.id.bt_add);
        bt_add.setOnClickListener(new ClickEvent());
        dop = new DatabaseOperation(this, db);
        
        lv_notes = (ListView)findViewById(R.id.lv_notes);
        
    }
    
    

    @Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		showNotesList();
        lv_notes.setOnItemClickListener(new ItemClickEvent());
        lv_notes.setOnItemLongClickListener(new ItemLongClickEvent());
    }
    private void showNotesList(){
        dop.create_db();
        Cursor cursor = dop.query_db();
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, 
        		R.layout.note_item,
        		cursor, 
        		new String[]{"_id","title","time"}, new int[]{R.id.tv_note_id,R.id.tv_note_title,R.id.tv_note_time});
        lv_notes.setAdapter(adapter);
        dop.close_db();
    	
    }

    class ItemLongClickEvent implements OnItemLongClickListener{

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {
			tv_note_id = (TextView)view.findViewById(R.id.tv_note_id);
			int item_id = Integer.parseInt(tv_note_id.getText().toString());
			simpleList(item_id);
			return true;
		}
    	
    }
    public void simpleList(final int item_id){
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this,R.style.custom_dialog);
		alertDialogBuilder.setTitle("NOTES");
		alertDialogBuilder.setIcon(R.drawable.ic_launcher_foreground);
		alertDialogBuilder.setItems(R.array.itemOperation, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

				switch(which){
					case 0 :
						Intent intent = new Intent(MainActivity.this,AddActivity.class);
						intent.putExtra("editModel", "update");
						intent.putExtra("noteId", item_id);
						startActivity(intent);
						break;
					case 1 :
						dop.create_db();
						dop.delete_db(item_id);
						dop.close_db();
						lv_notes.invalidate();
						showNotesList();
						break;
				}
			}
		});
		alertDialogBuilder.create();
		alertDialogBuilder.show();
	}

    class ItemClickEvent implements OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			tv_note_id = (TextView)view.findViewById(R.id.tv_note_id);
			int item_id = Integer.parseInt(tv_note_id.getText().toString());
			Intent intent = new Intent(MainActivity.this,AddActivity.class);
			intent.putExtra("editModel", "update");
			intent.putExtra("noteId", item_id);
			startActivity(intent);
		}
    }


	class ClickEvent implements OnClickListener{

		@Override
		public void onClick(View v) {
			switch(v.getId()){
			case R.id.bt_add :
				Intent intent = new Intent(MainActivity.this,AddActivity.class);
				intent.putExtra("editModel", "newAdd");
				startActivity(intent);
			}
		}	
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
