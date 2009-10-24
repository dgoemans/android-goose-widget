package com.davidgoemans.goosewidget;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import android.view.ViewDebug.FlagToString;
import android.widget.RemoteViews;

public class UpdateService extends Service 
{	
    @Override
    public void onStart(Intent intent, int startId) 
    {		
        RemoteViews updateViews = buildUpdate(this);
        
        AppWidgetManager manager = AppWidgetManager.getInstance(this);
        
        // Push update for all sized widgets to home screen       
        ComponentName thisWidget = new ComponentName(this, MedGooseWidget.class);
        manager.updateAppWidget(thisWidget, updateViews);

        thisWidget = new ComponentName(this, LargeGooseWidget.class);
        manager.updateAppWidget(thisWidget, updateViews);

    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public RemoteViews buildUpdate(Context context)
    {	            
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.main);
        Bitmap img = null;
        ComicData current = null;
        
        URL site;
		try
		{
			current = GetSelectedComic("http://abstrusegoose.com/feed", context);
			if( current == null ) throw new Exception("Null comic!");
				
			views.setTextViewText(R.id.message, current.title);
			
			Log.d("Goose Title", current.title );
			Log.d("Goose URL", current.imageURL );
			
			site = new URL(current.imageURL);
			
			HttpURLConnection conn= (HttpURLConnection)site.openConnection();
			conn.setDoInput(true);
			conn.setReadTimeout(5000);
			conn.connect();
			InputStream is = conn.getInputStream();

            Bitmap intermediate = BitmapFactory.decodeStream(is);
            float ar = (float)intermediate.getWidth()/(float)intermediate.getHeight();

            float size = 400.0f;
            
            if( intermediate.getWidth() < intermediate.getHeight() )
            {
            	img = Bitmap.createScaledBitmap(intermediate, (int) (size*ar), (int) size, false);
            }
            else
            {
            	img = Bitmap.createScaledBitmap(intermediate, (int)size, (int) (size/ar), false);
            }
            
            // Then create the content file for later!

            SharedPreferences prefs = getSharedPreferences(GooseWidget.PREFS_NAME, 0);
            SharedPreferences.Editor ed = prefs.edit();
            
            if( Environment.getExternalStorageState().equals( Environment.MEDIA_MOUNTED ) )
            {            
	            File outDir = new File("/sdcard/goose");
	            if( !outDir.exists() )
	            {
	            	outDir.mkdir();
	            }
	
	            Uri path;
	            if( site.getFile().length() == 0 )
	            {
	            	path = Uri.parse("/sdcard/goose/today.png");
	            }
	            else
	            {
	            	String file = site.getFile();
	            	int ind = file.lastIndexOf('/');
	            	file = file.substring(ind);
	            	path = Uri.parse("/sdcard/goose/" + file);
	            }
	            
	            File outFile = new File(path.toString());
	            
	            if (!outFile.exists())
	            {            	
	            	outFile.createNewFile();
	            }
	            
	            FileOutputStream out = new FileOutputStream(outFile);
	
	            intermediate.compress(CompressFormat.PNG, 7, out);
	            out.flush();
	            out.close();
				
				ed.putString("imagePath", "content://com.davidgoemans.goosewidgetprovider/"+ path.toString() );
				ed.commit();

            }
            else
            {
				ed.putString("imagePath", current.imageURL );
				ed.commit();
            }
            
		} 
    	catch (Exception e) 
    	{
			e.printStackTrace();
		}
		
        if( img != null )
        {
        	Log.d("img", img.toString() );
        	views.setImageViewBitmap(R.id.comic_image, img);	
        }
        else
        {
        	Log.d("img", "Failed to load");
        	views.setTextViewText(R.id.message, context.getString(R.string.widget_err));
        	views.setImageViewResource(R.id.comic_image, R.drawable.error);
        	
        	RunUpdateService failTimerTask = new RunUpdateService();
        	failTimerTask.context = context;        	
        	Timer timer = new Timer();
        	timer.schedule(failTimerTask, 180000);
        }

    	// When user taps, go to the menu
        Intent defineIntent = new Intent(context, GooseMenu.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0 /* no requestCode */, defineIntent, 0 /* no flags */);
        views.setOnClickPendingIntent(R.id.widget, pendingIntent);

        return views;
        
    }
    
    class ComicData
    {
    	public String title;
    	public String link;
    	public String imageURL;
    }
    
    public ComicData GetSelectedComic(String url, Context context) throws Exception
    {
    	URL site = new URL(url);
    	
    	HttpURLConnection conn= (HttpURLConnection)site.openConnection();
		conn.setDoInput(true);
    	conn.connect();
    	
    	Document doc = null;
    	
    	try
    	{
        	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        	DocumentBuilder db = dbf.newDocumentBuilder();
        	doc = db.parse(conn.getInputStream());
    	}
    	catch( Exception e )
    	{
    		Log.e("Errror", e.getMessage());
    	}
    	
    	NodeList itemsXML = doc.getElementsByTagName("item");
    	
    	SharedPreferences prefs = getSharedPreferences(GooseWidget.PREFS_NAME, 0);
    	int toGet = prefs.getInt("comicPage", 0);
		
    	toGet = Math.min(toGet, itemsXML.getLength()-1);
    	toGet = Math.max(toGet, 0);
    	
    	SharedPreferences.Editor ed = prefs.edit();
    	ed.putInt("comicPage", toGet);
    	ed.putInt("availPages", itemsXML.getLength() );
    	ed.commit();
    	
    	Log.d("Goose", "Page: " + toGet);
    	Log.d("Goose", "of: " + itemsXML.getLength());
    	
		Element current = (Element)itemsXML.item(toGet);
		Node title = (Node)current.getElementsByTagName("title").item(0);
		Node link = (Node)current.getElementsByTagName("link").item(0);
		Node img = (Node)current.getElementsByTagName("content:encoded").item(0);
		
		ComicData data = new ComicData();
		
		data.title = title.getFirstChild().getNodeValue();
		data.link = link.getFirstChild().getNodeValue();
		
		String cdata = img.getFirstChild().getNodeValue();
		int startOfUrl = cdata.indexOf("http://abstrusegoose.com/strips/");
		int endOfUrl = cdata.indexOf("\"", startOfUrl+1);
		String actualUrl = cdata.substring(startOfUrl, endOfUrl);
		data.imageURL = actualUrl;
    		
		return data;
    }
    
    
    class RunUpdateService extends TimerTask 
    {
    	public Context context = null;    	
    	
    	public void run() 
    	{
    		Log.d("Goose", "task!");
    		if( context != null )
    		{
    			startService(new Intent(context, UpdateService.class));
    		}
    	}
    }
}
