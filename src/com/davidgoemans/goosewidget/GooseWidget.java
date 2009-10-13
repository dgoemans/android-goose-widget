package com.davidgoemans.goosewidget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;


public class GooseWidget extends AppWidgetProvider 
{
	public static final String PREFS_NAME = "gooseprefs";

//	@Override
//	public void onEnabled(Context context) 
//	{
//		/*
//        final Button button = (Button) findViewById(R.id.zin);
//        button.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                // Perform action on click
//            }
//        });
//        */
//		
//	}
	
 	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) 
 	{
        context.startService(new Intent(context, UpdateService.class));
    }
}