package com.davidgoemans.goosewidget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;


public class GooseWidget extends AppWidgetProvider 
{
	public static final String PREFS_NAME = "gooseprefs";
	
 	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) 
 	{
        context.startService(new Intent(context, UpdateService.class));
    }
}