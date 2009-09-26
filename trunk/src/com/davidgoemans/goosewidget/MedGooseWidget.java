package com.davidgoemans.goosewidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MedGooseWidget extends GooseWidget 
{
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) 
 	{
        context.startService(new Intent(context, UpdateService.class));
 	}
}
