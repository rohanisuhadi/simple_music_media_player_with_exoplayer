// Copyright 2016 Google Inc.
// 
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
//      http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.example.kamtiz.musicplayersample;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.app.NotificationCompat.MediaStyle;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.bumptech.glide.Glide;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;

/**
 * Keeps track of a notification and updates it automatically for a given
 * MediaSession. This is required so that the music service
 * don't get killed during playback.
 */
public class MediaNotificationManager extends BroadcastReceiver {
    private static final int NOTIFICATION_ID = 412;
    private static final int REQUEST_CODE = 100;

    private static final String ACTION_PAUSE = "com.example.android.musicplayercodelab.pause";
    private static final String ACTION_PLAY = "com.example.android.musicplayercodelab.play";
    private static final String ACTION_NEXT = "com.example.android.musicplayercodelab.next";
    private static final String ACTION_PREV = "com.example.android.musicplayercodelab.prev";
    private static final String ACTION_STOP = "com.example.android.musicplayercodelab.stop";
    private static final String ACTION_STOP_CASTING = "com.example.android.musicplayercodelab.stop_cast";

    private final MusicService mService;
    private final NotificationManager mNotificationManager;
    private final NotificationCompat.Action mPlayAction;
    private final NotificationCompat.Action mPauseAction;
    private final NotificationCompat.Action mNextAction;
    private final NotificationCompat.Action mPrevAction;


    private final PendingIntent playIntent;
    private final PendingIntent pauseIntent;
    private final PendingIntent prevIntent;
    private final PendingIntent nextIntent;
    private final PendingIntent mStopIntent;

    private final PendingIntent mStopCastIntent;



    private boolean mStarted;
    private Bitmap iconLargeBitmap;

    public MediaNotificationManager(MusicService service) {
        mService = service;

        String pkg = mService.getPackageName();
        playIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_PLAY).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        pauseIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_PAUSE).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        nextIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_NEXT).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        prevIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_PREV).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mStopIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_STOP).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mStopCastIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_STOP_CASTING).setPackage(pkg),
                PendingIntent.FLAG_CANCEL_CURRENT);

        mPlayAction = new NotificationCompat.Action(R.drawable.ic_play_arrow_white_24dp,
                mService.getString(R.string.label_play), playIntent);
        mPauseAction = new NotificationCompat.Action(R.drawable.ic_pause_white_24dp,
                mService.getString(R.string.label_pause), pauseIntent);
        mNextAction = new NotificationCompat.Action(R.drawable.ic_skip_next_white_24dp,
                mService.getString(R.string.label_next), nextIntent);
        mPrevAction = new NotificationCompat.Action(R.drawable.ic_skip_previous_white_24dp,
                mService.getString(R.string.label_previous), prevIntent);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_NEXT);
        filter.addAction(ACTION_PAUSE);
        filter.addAction(ACTION_PLAY);
        filter.addAction(ACTION_PREV);
        filter.addAction(ACTION_STOP_CASTING);

        mService.registerReceiver(this, filter);


        mNotificationManager = (NotificationManager) mService
                .getSystemService(Context.NOTIFICATION_SERVICE);

        // Cancel all notifications to handle the case where the Service was killed and
        // restarted by the system.
        mNotificationManager.cancelAll();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        switch (action) {
            case ACTION_PAUSE:
                mService.mCallback.onPause();
                break;
            case ACTION_PLAY:
                mService.mCallback.onPlay();
                break;
            case ACTION_NEXT:
                mService.mCallback.onSkipToNext();
                break;
            case ACTION_PREV:
                mService.mCallback.onSkipToPrevious();
                break;
            case ACTION_STOP_CASTING:
                Intent i = new Intent(context, MusicService.class);
                i.setAction(MusicService.ACTION_CMD);
                i.putExtra(MusicService.CMD_NAME, MusicService.CMD_STOP_CASTING);
                mService.startService(i);
                break;
        }
    }

    public void update(MediaMetadataCompat metadata, PlaybackStateCompat state, MediaSessionCompat.Token token) {
        if (state == null || state.getState() == PlaybackStateCompat.STATE_STOPPED ||
                state.getState() == PlaybackStateCompat.STATE_NONE) {
            mService.stopForeground(true);
            try {
                mService.unregisterReceiver(this);
            } catch (IllegalArgumentException ex) {
                // ignore receiver not registered
            }
            mService.stopSelf();
            return;
        }
        if (metadata == null) {
            return;
        }

        MediaDescriptionCompat description = metadata.getDescription();
        String ulrImage = MusicLibrary.getAlbumBitmap(mService,description.getMediaId());

        new AsyncTask<Void,Void,Void>(){

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    Log.e("ULRIMAGE",ulrImage);
                    iconLargeBitmap = Glide.with(mService).asBitmap().load(ulrImage).into(256,256).get();
                }catch (ExecutionException e){
                    Log.e("ErrorLoad","ExecutionException");
                } catch (InterruptedException e) {
                    Log.e("ErrorLoad","InterruptedException");
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                Log.e("ErrorLoad","Berhasil");
                setNotification(description, state, token);
            }
        }.execute();

    }

    private void setNotification(MediaDescriptionCompat description, PlaybackStateCompat state, MediaSessionCompat.Token token){
        boolean isPlaying = state.getState() == PlaybackStateCompat.STATE_PLAYING;
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mService);

        notificationBuilder
                .setStyle(new MediaStyle()
                        .setMediaSession(token)
                        .setShowActionsInCompactView(0, 1, 2))

                .setColor(mService.getApplication().getResources().getColor(R.color.notification_bg))
                .setSmallIcon(R.drawable.ic_notification)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(createContentIntent())
                .setContentTitle(description.getTitle())
                .setContentText(description.getSubtitle())
                .setLargeIcon(iconLargeBitmap)
                .setOngoing(isPlaying)
                .setWhen(isPlaying ? System.currentTimeMillis() - state.getPosition() : 0)
                .setShowWhen(isPlaying)
                .setUsesChronometer(isPlaying);


        // If skip to next action is enabled
        if ((state.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS) != 0) {
            notificationBuilder.addAction(mPrevAction);
        }

        notificationBuilder.addAction(isPlaying ? mPauseAction : mPlayAction);

        // If skip to prev action is enabled
        if ((state.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_NEXT) != 0) {
            notificationBuilder.addAction(mNextAction);
        }

        Notification notification = notificationBuilder.build();

        if (isPlaying && !mStarted) {
            mService.startService(new Intent(mService.getApplicationContext(), MusicService.class));
            mService.startForeground(NOTIFICATION_ID, notification);
            mStarted = true;
        } else {
            if (!isPlaying) {
                mService.stopForeground(false);
                mStarted = false;
            }
            mNotificationManager.notify(NOTIFICATION_ID, notification);
        }
    }

    private void loadImage(String ulrImage){

    }

    private PendingIntent createContentIntent() {
        Intent openUI = new Intent(mService, MainActivity.class);
        openUI.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(mService, REQUEST_CODE, openUI,
                PendingIntent.FLAG_CANCEL_CURRENT);
    }


}