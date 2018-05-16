package com.example.kamtiz.musicplayersample;

import android.app.Activity;
import android.content.ComponentName;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    
    private final String TAG = MainActivity.class.getName();

    private BrowseAdapter mBrowserAdapter;
    private ImageButton mPlayPause;
    private TextView mTitle;
    private TextView mSubtitle;
    private ImageView mAlbumArt;
    private ViewGroup mPlaybackControls;

    private MediaMetadataCompat mCurrentMetadata;
    private PlaybackStateCompat mCurrentState;

    private MediaBrowserCompat mMediaBrowser;



    private final MediaBrowserCompat.ConnectionCallback mConnectionCallback =
            new MediaBrowserCompat.ConnectionCallback() {
                @Override
                public void onConnected() {
                    try {
                        connectToSession(mMediaBrowser.getSessionToken());
                    } catch (RemoteException e) {
                        Log.e(TAG, "could not connect media controller");
                    }
                }
            };

    private void connectToSession(MediaSessionCompat.Token token) throws RemoteException {
        Log.e("Data", mMediaBrowser.getRoot());
        if(mMediaBrowser != null){
            mMediaBrowser.subscribe(mMediaBrowser.getRoot(), mSubscriptionCallback);
        }
        MediaControllerCompat mediaController = new MediaControllerCompat(MainActivity.this, token);
        Log.e("Berjalan","Datang");
        updatePlaybackState(mediaController.getPlaybackState());
        updateMetadata(mediaController.getMetadata());
        mediaController.registerCallback(mMediaControllerCallback);
        MediaControllerCompat.setMediaController(MainActivity.this, mediaController);

//        if (mediaController.getMetadata() == null) {
//            finish();
//            Log.e("Berjalan","Stop");
//            return;
//        }else {
//            Log.e("Berjalan","Datang");
//            updatePlaybackState(mediaController.getPlaybackState());
//            updateMetadata(mediaController.getMetadata());
//            mediaController.registerCallback(mMediaControllerCallback);
//            MediaControllerCompat.setMediaController(MainActivity.this, mediaController);
//        }


    }

    // Receive callbacks from the MediaController. Here we update our state such as which queue
    // is being shown, the current title and description and the PlaybackState.
    private final MediaControllerCompat.Callback mMediaControllerCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            updateMetadata(metadata);
            mBrowserAdapter.notifyDataSetChanged();
        }

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            updatePlaybackState(state);
            mBrowserAdapter.notifyDataSetChanged();
        }

        @Override
        public void onSessionDestroyed() {
            updatePlaybackState(null);
            mBrowserAdapter.notifyDataSetChanged();
        }
    };

    private final MediaBrowserCompat.SubscriptionCallback mSubscriptionCallback =
            new MediaBrowserCompat.SubscriptionCallback() {
                @Override
                public void onChildrenLoaded(@NonNull String parentId,@NonNull List<MediaBrowserCompat.MediaItem> children) {
                    try {
                        mBrowserAdapter.clear();
                        mBrowserAdapter.addAll(children);
                        mBrowserAdapter.notifyDataSetChanged();

                    } catch (Throwable t) {
                        Log.e(TAG, "Error on childrenloaded", t);
                    }
                }

                @Override
                public void onError(@NonNull String id) {
                    Log.e(TAG, "browse fragment subscription onError, id=" + id);
                }
            };


    private void onMediaItemSelected(MediaBrowserCompat.MediaItem item) {
        if (item.isPlayable()) {
//            getMediaController().getTransportControls().playFromMediaId(item.getMediaId(), null);
            MediaControllerCompat.TransportControls controls = MediaControllerCompat.getMediaController(MainActivity.this).getTransportControls();
            controls.playFromMediaId(item.getMediaId(), null);
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(getString(R.string.app_name));


        mBrowserAdapter = new BrowseAdapter(this);

        ListView listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(mBrowserAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MediaBrowserCompat.MediaItem item = mBrowserAdapter.getItem(position);
                onMediaItemSelected(item);
            }
        });

        // Playback controls configuration:
        mPlaybackControls = (ViewGroup) findViewById(R.id.playback_controls);
        mPlayPause = (ImageButton) findViewById(R.id.play_pause);
        mPlayPause.setEnabled(true);
        mPlayPause.setOnClickListener(mPlaybackButtonListener);

        mTitle = (TextView) findViewById(R.id.title);
        mSubtitle = (TextView) findViewById(R.id.artist);
        mAlbumArt = (ImageView) findViewById(R.id.album_art);
    }


    @Override
    public void onStart() {
        super.onStart();
        mMediaBrowser = new MediaBrowserCompat(this,new ComponentName(this, MusicService.class), mConnectionCallback, null);
        mMediaBrowser.connect();
    }

    @Override
    public void onStop() {
        super.onStop();

        MediaControllerCompat controllerCompat = MediaControllerCompat.getMediaController(MainActivity.this);
        if (controllerCompat != null) {
            controllerCompat.unregisterCallback(mMediaControllerCallback);
        }

        if (mMediaBrowser != null && mMediaBrowser.isConnected()) {
            String data = mCurrentMetadata.getDescription().getMediaId();
            if(data==null){
                data = mMediaBrowser.getRoot();
            }
            mMediaBrowser.unsubscribe(data);
        }
    }

    private void updatePlaybackState(PlaybackStateCompat state) {
        mCurrentState = state;
        if (state == null || state.getState() == PlaybackStateCompat.STATE_PAUSED ||
                state.getState() == PlaybackStateCompat.STATE_STOPPED) {
            mPlayPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_arrow_black_36dp));
        } else {
            mPlayPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_black_36dp));
        }
        mPlaybackControls.setVisibility(state == null ? View.GONE : View.VISIBLE);
    }

    private void updateMetadata(MediaMetadataCompat metadata) {
        mCurrentMetadata = metadata;
        mTitle.setText(metadata == null ? "" : metadata.getDescription().getTitle());
        mSubtitle.setText(metadata == null ? "" : metadata.getDescription().getSubtitle());
//        mAlbumArt.setImageBitmap(metadata == null ? null : MusicLibrary.getAlbumBitmap(this,
//                metadata.getDescription().getMediaId()));
        if(metadata != null ){
            Glide.with(this).load(MusicLibrary.getAlbumBitmap(MainActivity.this,
                    metadata.getDescription().getMediaId()))
                    .apply(new RequestOptions().placeholder(R.drawable.ic_launcher)).into(mAlbumArt);

        }

        mBrowserAdapter.notifyDataSetChanged();
    }

    // An adapter for showing the list of browsed MediaItem's
    private class BrowseAdapter extends ArrayAdapter<MediaBrowserCompat.MediaItem> {

        public BrowseAdapter(Activity context) {
            super(context, R.layout.media_list_item, new ArrayList<MediaBrowserCompat.MediaItem>());
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            MediaBrowserCompat.MediaItem item = getItem(position);
            int itemState = MediaItemViewHolder.STATE_NONE;
            if (item.isPlayable()) {
                String itemMediaId = item.getDescription().getMediaId();
                int playbackState = PlaybackStateCompat.STATE_NONE;
                if (mCurrentState != null) {
                    playbackState = mCurrentState.getState();
                }
                if (mCurrentMetadata != null &&
                        itemMediaId.equals(mCurrentMetadata.getDescription().getMediaId())) {
                    if (playbackState == PlaybackStateCompat.STATE_PLAYING ||
                            playbackState == PlaybackStateCompat.STATE_BUFFERING) {
                        itemState = MediaItemViewHolder.STATE_PLAYING;
                    } else if (playbackState != PlaybackStateCompat.STATE_ERROR) {
                        itemState = MediaItemViewHolder.STATE_PAUSED;
                    }
                }
            }
            return MediaItemViewHolder.setupView((Activity) getContext(), convertView, parent,
                    item.getDescription(), itemState);
        }
    }

    private final View.OnClickListener mPlaybackButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final int state = mCurrentState == null ?
                    PlaybackStateCompat.STATE_NONE : mCurrentState.getState();
            MediaControllerCompat.TransportControls controls = MediaControllerCompat.getMediaController(MainActivity.this).getTransportControls();

            if (state == PlaybackStateCompat.STATE_PAUSED ||
                    state == PlaybackStateCompat.STATE_STOPPED ||
                    state == PlaybackStateCompat.STATE_NONE) {

                if (mCurrentMetadata == null) {
                    mCurrentMetadata = MusicLibrary.getMetadata(MainActivity.this,
                            MusicLibrary.getMediaItems().get(0).getMediaId());
                    updateMetadata(mCurrentMetadata);
                }
                controls.playFromMediaId(
                        mCurrentMetadata.getDescription().getMediaId(), null);

            } else {
                controls.pause();
            }
        }
    };
}
