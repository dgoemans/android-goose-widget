package com.davidgoemans.goosewidget;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

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
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.IBinder;
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
			current = GetLatestComic("http://abstrusegoose.com/feed", context);
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
            
            Log.d("Aspect Ratio:", Float.toString(ar) );
            Log.d("Width:", Float.toString(intermediate.getWidth()) );
            Log.d("Height:", Float.toString(intermediate.getHeight()) );

            float size = 400.0f;
            
            img = Bitmap.createScaledBitmap(intermediate, (int) (size*ar), (int) size, false);
            
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
        }

    	// When user taps, go to the goose!
        Intent defineIntent = new Intent(context, GooseMenu.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                0 /* no requestCode */, defineIntent, 0 /* no flags */);
        views.setOnClickPendingIntent(R.id.widget, pendingIntent);

        return views;
        
    }
    
    class ComicData
    {
    	public String title;
    	public String link;
    	public String imageURL;
    }
    
    public ComicData GetLatestComic(String url, Context context) throws Exception
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
    	for( int i=0; i < itemsXML.getLength(); i++ )
    	{
    		Element current = (Element)itemsXML.item(i);
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
		return null;    	
    }
}
