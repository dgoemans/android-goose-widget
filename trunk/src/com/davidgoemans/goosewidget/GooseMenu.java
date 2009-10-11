package com.davidgoemans.goosewidget;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;

public class GooseMenu extends ListActivity
{		
	private String[] menuEntries;
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{		
		menuEntries = new String[5];
		menuEntries[0] = getString(R.string.menu_ref);
		
		Log.d("Goose", String.valueOf( android.os.Environment.getExternalStorageState() ) );
		
		if( android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED) )
		{
			menuEntries[1] = getString(R.string.menu_view);
		}
		else
		{
			menuEntries[1] = getString(R.string.menu_view_broken);
		}
			
		menuEntries[2] = getString(R.string.menu_go);
		menuEntries[3] = getString(R.string.menu_feed);
		menuEntries[4] = getString(R.string.menu_dev);
		
		
		super.onCreate(savedInstanceState);
		setListAdapter(new ArrayAdapter<String>(this,R.layout.menu_item, menuEntries));
		
		

	}

	@Override
	protected void onListItemClick(android.widget.ListView l, android.view.View v, int position, long id )
	{
		super.onListItemClick(l, v, position, id);

		Uri uri;
		switch( position )
		{
		case 0:
			this.startService(new Intent(this, UpdateService.class));
			this.finish();
			break;
		case 1:
			
			try
			{			
				SharedPreferences prefs = getSharedPreferences(GooseWidget.PREFS_NAME, 0);
				String path = prefs.getString("imagePath", "");
				
				uri = Uri.parse(path);
				
				//File f = new File(super.getFilesDir()+"/goose_today.png");
				
				/*ContentValues values = new ContentValues(3);
				values.put(Media.DISPLAY_NAME, "Goose");
				values.put(Media.DESCRIPTION, "Today's Goose");
				values.put(Media.MIME_TYPE, "image/png");
				
				Uri imageUri = getContentResolver().insert(Media.EXTERNAL_CONTENT_URI, values);
				
				OutputStream outStream = getContentResolver().openOutputStream(imageUri);
				outStream.write( f. )
			    sourceBitmap.compress(Bitmap.CompressFormat.JPEG, 50, outStream);
			    outStream.close();*/
				
				startActivity(new Intent(Intent.ACTION_VIEW, uri));
			}
			catch( Exception e)
			{
				Log.e("Goose", "Exception occured" + e.toString());
			}
			this.finish();
			break;
		case 2:
			uri = Uri.parse("http://abstrusegoose.com");
			startActivity(new Intent(Intent.ACTION_VIEW, uri));
			this.finish();
			break;
		case 3:
			uri = Uri.parse("http://abstrusegoose.com/feedthegoose");
			startActivity(new Intent(Intent.ACTION_VIEW, uri));
			this.finish();
			break;
		case 4:
			uri = Uri.parse("http://davidgoemans.com/");
			startActivity(new Intent(Intent.ACTION_VIEW, uri));
			this.finish();
			break;
		}
	}
		/*Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);

        String action = getIntent().getAction();
        if (Intent.ACTION_PICK.equals(action) || Intent.ACTION_GET_CONTENT.equals(action)) {
            // The caller is waiting for us to return a note selected by
            // the user.  The have clicked on one, so return it now.
            setResult(RESULT_OK, new Intent().setData(uri));
        } else {
            // Launch activity to view/edit the currently selected item
            startActivity(new Intent(Intent.ACTION_EDIT, uri));
        }*/	
	//}
}