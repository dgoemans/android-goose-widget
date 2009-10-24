package com.davidgoemans.goosewidget;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import android.widget.ArrayAdapter;

public class GooseMenu extends ListActivity
{		
	private List<String> menuEntries;
	private int page;
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		menuEntries = new ArrayList<String>();
		menuEntries.add( getString(R.string.menu_ref) );
		
		SharedPreferences prefs = getSharedPreferences(GooseWidget.PREFS_NAME, 0);
		page = prefs.getInt("comicPage", 0);
		
		
		menuEntries.add( getString(R.string.menu_view) );
		menuEntries.add( getString(R.string.menu_go) );
		menuEntries.add( getString(R.string.menu_feed) );
		menuEntries.add( getString(R.string.menu_dev) );
		
		if( page+1 < prefs.getInt("availPages", 0) )
		{
			menuEntries.add( getString(R.string.menu_prev) );
		}
		
		if( page > 0 )
		{
			menuEntries.add( getString(R.string.menu_next) );
		}
		
		super.onCreate(savedInstanceState);
		setListAdapter(new ArrayAdapter<String>(this,R.layout.menu_item, menuEntries));		
		

	}
	
	@Override
	protected void onListItemClick(android.widget.ListView l, android.view.View v, int position, long id )
	{
		super.onListItemClick(l, v, position, id);

		SharedPreferences prefs = getSharedPreferences(GooseWidget.PREFS_NAME, 0);
		SharedPreferences.Editor ed = prefs.edit();

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
				prefs = getSharedPreferences(GooseWidget.PREFS_NAME, 0);
				String path = prefs.getString("imagePath", "");
				
				uri = Uri.parse(path);
				
				Intent pending = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(pending);
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
		case 5:
			if( menuEntries.get(5).equals(getString(R.string.menu_next)) )
			{
				DecPage();
			}
			else
			{
				IncPage();
			}
			this.startService(new Intent(this, UpdateService.class));
			this.finish();
			break;
		case 6:
			DecPage();
			this.startService(new Intent(this, UpdateService.class));
			this.finish();
			break;
		}
	}
	
	void DecPage()
	{
		SharedPreferences prefs = getSharedPreferences(GooseWidget.PREFS_NAME, 0);
		SharedPreferences.Editor ed = prefs.edit();
		
		if( page > 0 )
		{
			page--;
			ed.putInt("comicPage", page );
			ed.commit();
		}	
	}
	
	void IncPage()
	{
		SharedPreferences prefs = getSharedPreferences(GooseWidget.PREFS_NAME, 0);
		SharedPreferences.Editor ed = prefs.edit();

		if( page + 1 < prefs.getInt("availPages", 0 ) )
		{
			page++;
			ed = prefs.edit();
			ed.putInt("comicPage", page );
			ed.commit();
		}
	}
}
