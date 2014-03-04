package com.klinker.android.twitter.widget;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.klinker.android.twitter.R;
import com.klinker.android.twitter.services.WidgetRefreshService;
import com.klinker.android.twitter.ui.MainActivity;
import com.klinker.android.twitter.ui.compose.WidgetCompose;
import com.klinker.android.twitter.ui.tweet_viewer.TweetActivityWidget;
import com.klinker.android.twitter.utils.redirects.RedirectToDMs;
import com.klinker.android.twitter.utils.redirects.RedirectToMentions;

/**
 * Created by luke on 3/4/14.
 */
public class UnreadWidgetProvider extends AppWidgetProvider {

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Intent updateWidget = new Intent(context, UnreadWidgetService.class);
        context.startService(updateWidget);

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("com.klinker.android.messaging.UPDATE_WIDGET")) {
            Intent updateWidget = new Intent(context, UnreadWidgetService.class);
            context.startService(updateWidget);
        } else {
            super.onReceive(context, intent);
        }
    }

    public static class UnreadWidgetService extends IntentService {
        public UnreadWidgetService() {
            super("card_widget_service");
        }

        @Override
        public IBinder onBind(Intent arg0) {
            return null;
        }

        @Override
        protected void onHandleIntent(Intent arg0) {
            AppWidgetManager mgr = AppWidgetManager.getInstance(this);
            ComponentName thisAppWidget = new ComponentName(this.getPackageName(), UnreadWidgetProvider.class.getName());
            int[] appWidgetIds = mgr.getAppWidgetIds(thisAppWidget);

            Log.v("talon_unread_widget", "running service");

            int res = 0;
            /*switch (Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString("widget_theme", "3"))) {
                case 0:
                    res = R.layout.widget_light;
                    break;
                case 1:
                    res = R.layout.widget_dark;
                    break;
                case 2:
                    res = R.layout.widget_trans_light;
                    break;
                case 3:
                    res = R.layout.widget_trans_black;
                    break;
            }*/
            res = R.layout.widget_unread_trans_black;

            RemoteViews views = new RemoteViews(this.getPackageName(), res);

            for (int i = 0; i < appWidgetIds.length; i++) {
                Log.v("talon_unread_widget", "in for loop");
                int appWidgetId = appWidgetIds[i];

                Intent quickText = new Intent(this, WidgetCompose.class);
                quickText.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                PendingIntent quickPending = PendingIntent.getActivity(this, 0, quickText, 0);

                Intent intent2 = new Intent(this, WidgetService.class);
                intent2.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                intent2.setData(Uri.parse(intent2.toUri(Intent.URI_INTENT_SCHEME)));

                Intent openApp = new Intent(this, MainActivity.class);
                PendingIntent openAppPending = PendingIntent.getActivity(this, 0, openApp, 0);

                Intent mentions = new Intent(this, RedirectToMentions.class);
                PendingIntent mentionsPending = PendingIntent.getService(this, 0, mentions, 0);

                Intent dms = new Intent(this, RedirectToDMs.class);
                PendingIntent dmsPending = PendingIntent.getService(this, 0, dms, 0);

                views.setOnClickPendingIntent(R.id.launcherIcon, openAppPending);
                views.setOnClickPendingIntent(R.id.replyButton, quickPending);
                views.setOnClickPendingIntent(R.id.timeline, openAppPending);
                views.setOnClickPendingIntent(R.id.mentions, mentionsPending);
                views.setOnClickPendingIntent(R.id.dms, dmsPending);

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                mgr.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widgetList);
            }

            stopSelf();
        }
    }
}
/*extends AppWidgetProvider {

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Intent updateWidget = new Intent(context, UnreadWidgetService.class);
        context.startService(updateWidget);

        super.onUpdate(context, appWidgetManager, appWidgetIds);

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("com.klinker.android.talon.UPDATE_WIDGET")) {
            Intent updateWidget = new Intent(context, UnreadWidgetService.class);
            context.startService(updateWidget);
        } else {
            super.onReceive(context, intent);
        }
    }

    public static class UnreadWidgetService extends IntentService {
        public UnreadWidgetService() {
            super("unread_widget_service");
        }

        @Override
        public IBinder onBind(Intent arg0) {
            return null;
        }

        @Override
        protected void onHandleIntent(Intent arg0) {
            AppWidgetManager mgr = AppWidgetManager.getInstance(this);
            ComponentName thisAppWidget = new ComponentName(this.getPackageName(), UnreadWidgetProvider.class.getName());
            int[] appWidgetIds = mgr.getAppWidgetIds(thisAppWidget);

            int res = 0;
            switch (Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString("widget_theme", "3"))) {
                case 0:
                    res = R.layout.widget_light;
                    break;
                case 1:
                    res = R.layout.widget_dark;
                    break;
                case 2:
                    res = R.layout.widget_trans_light;
                    break;
                case 3:
                    res = R.layout.widget_trans_black;
                    break;
            }
            res = R.layout.widget_unread_trans_black;

            RemoteViews views = new RemoteViews(this.getPackageName(), res);
            views.setViewVisibility(R.id.replyButton, View.VISIBLE);

            for (int i = 0; i < appWidgetIds.length; i++) {
                mgr.updateAppWidget(appWidgetIds[i], views);
            }

            for (int i = 0; i < appWidgetIds.length; i++) {
                int appWidgetId = appWidgetIds[i];

                /*Intent quickText = new Intent(this, WidgetCompose.class);
                quickText.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                PendingIntent quickPending = PendingIntent.getActivity(this, 0, quickText, 0);

                Intent intent2 = new Intent(this, WidgetService.class);
                intent2.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                intent2.setData(Uri.parse(intent2.toUri(Intent.URI_INTENT_SCHEME)));

                Intent openApp = new Intent(this, MainActivity.class);
                PendingIntent openAppPending = PendingIntent.getActivity(this, 0, openApp, 0);

                Intent mentions = new Intent(this, RedirectToMentions.class);
                PendingIntent mentionsPending = PendingIntent.getService(this, 0, mentions, 0);

                Intent dms = new Intent(this, RedirectToDMs.class);
                PendingIntent dmsPending = PendingIntent.getService(this, 0, dms, 0);

                views.setRemoteAdapter(R.id.widgetList, intent2);
                views.setEmptyView(R.id.widgetList, R.color.light_background);

                views.setOnClickPendingIntent(R.id.launcherIcon, openAppPending);
                views.setOnClickPendingIntent(R.id.replyButton, quickPending);
                views.setOnClickPendingIntent(R.id.timeline, openAppPending);
                views.setOnClickPendingIntent(R.id.mentions, mentionsPending);
                views.setOnClickPendingIntent(R.id.dms, dmsPending);*/

                /*Intent openIntent = new Intent(this, UnreadWidgetProvider.class);
                openIntent.setAction("OPEN_APP");
                openIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
                openIntent.setData(Uri.parse(openIntent.toUri(Intent.URI_INTENT_SCHEME)));
                PendingIntent openPendingIntent = PendingIntent.getBroadcast(this, 0, openIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                views.setPendingIntentTemplate(R.id.widgetList, openPendingIntent);*/

                /*mgr.updateAppWidget(appWidgetId, views);

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //mgr.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widgetList);
            }

            stopSelf();
        }

    }
}*/