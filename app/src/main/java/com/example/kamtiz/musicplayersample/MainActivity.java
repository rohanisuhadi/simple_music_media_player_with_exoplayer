package com.example.kamtiz.musicplayersample;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements MediaBrowserFragment.MediaFragmentListener{
    
    private final String TAG = MainActivity.class.getName();

    public static final String EXTRA_CURRENT_MEDIA_DESCRIPTION =
            "com.example.kamtiz.musicplayersample.CURRENT_MEDIA_DESCRIPTION";

    private BrowseAdapter mBrowserAdapter;
    private ImageButton mPlayPause;
    private TextView mTitle;
    private TextView mSubtitle;
    private ImageView mAlbumArt;
    private ViewGroup mPlaybackControls;

    private MediaMetadataCompat mCurrentMetadata;
    private PlaybackStateCompat mCurrentState;
    private MediaBrowserCompat mMediaBrowser;

    private MusicLibrary musicLibrary;


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
                        Log.e("Masukdata","bener");
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


    public void onMediaItemSelected(MediaBrowserCompat.MediaItem item) {
        if (item.isPlayable()) {
            MediaControllerCompat.TransportControls controls = MediaControllerCompat.getMediaController(MainActivity.this).getTransportControls();
            controls.playFromMediaId(item.getMediaId(), null);
        }
    }

    @Override
    public MediaMetadataCompat getMediaMetadataCompat() {
        return this.mCurrentMetadata;
    }

    @Override
    public PlaybackStateCompat getPlaybackStateCompat() {
        return this.mCurrentState;
    }

    @Override
    public MediaBrowserCompat getMediaBrowser() {
        return this.mMediaBrowser;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(getString(R.string.app_name));

        mBrowserAdapter = new BrowseAdapter(this);
        musicLibrary = new MusicLibrary();

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
        mMediaBrowser = new MediaBrowserCompat(this,new ComponentName(this, MusicService.class), mConnectionCallback, null);
        musicLibrary.createMediaMetadata("sembuhkan_aku", "Sembuhkan aku dari selingkuh",
                "Media Right Productions", "Jazz & Blues", "Jazz", 103,
                "http://audiobookchapters.kilatstorage.com/3554_20170116234735.mp3",
                "http://audiobookchaptercoverarts.kilatstorage.com/3554_20161227195825.jpg",
                "album_jazz_blues");
        musicLibrary.createMediaMetadata("1",
                "The Coldest Shoulder", "The 126ers", "Youtube Audio Library Rock 2", "Rock", 160,
                "http://audiobookpreviews.kilatstorage.com/2929_20180321053709.mp3",
                "http://audiobookchaptercoverarts.kilatstorage.com/6696_20180417152054.png",
                "album_youtube_audio_library_rock_2");
        musicLibrary.createMediaMetadata("2",
                "The Coldest Shoulder", "The 126ers", "Youtube Audio Library Rock 2", "Rock", 160,
                "http://audiobookpreviews.kilatstorage.com/2929_20180321053709.mp3",
                "http://audiobookchaptercoverarts.kilatstorage.com/6696_20180417152054.png",
                "album_youtube_audio_library_rock_2");
        musicLibrary.createMediaMetadata("3",
                "The Coldest Shoulder", "The 126ers", "Youtube Audio Library Rock 2", "Rock", 160,
                "http://audiobookpreviews.kilatstorage.com/2929_20180321053709.mp3",
                "http://audiobookchaptercoverarts.kilatstorage.com/6696_20180417152054.png",
                "album_youtube_audio_library_rock_2");
        musicLibrary.createMediaMetadata("4",
                "The Coldest Shoulder", "The 126ers", "Youtube Audio Library Rock 2", "Rock", 160,
                "http://audiobookpreviews.kilatstorage.com/2929_20180321053709.mp3",
                "http://audiobookchaptercoverarts.kilatstorage.com/6696_20180417152054.png",
                "album_youtube_audio_library_rock_2");
        musicLibrary.createMediaMetadata("5",
                "The Coldest Shoulder", "The 126ers", "Youtube Audio Library Rock 2", "Rock", 160,
                "http://audiobookpreviews.kilatstorage.com/2929_20180321053709.mp3",
                "http://audiobookchaptercoverarts.kilatstorage.com/6696_20180417152054.png",
                "album_youtube_audio_library_rock_2");
        musicLibrary.createMediaMetadata("6",
                "The Coldest Shoulder", "The 126ers", "Youtube Audio Library Rock 2", "Rock", 160,
                "http://audiobookpreviews.kilatstorage.com/2929_20180321053709.mp3",
                "http://audiobookchaptercoverarts.kilatstorage.com/6696_20180417152054.png",
                "album_youtube_audio_library_rock_2");
        musicLibrary.createMediaMetadata("7",
                "The Coldest Shoulder", "The 126ers", "Youtube Audio Library Rock 2", "Rock", 160,
                "http://audiobookpreviews.kilatstorage.com/2929_20180321053709.mp3",
                "http://audiobookchaptercoverarts.kilatstorage.com/6696_20180417152054.png",
                "album_youtube_audio_library_rock_2");
        musicLibrary.createMediaMetadata("9",
                "The Coldest Shoulder", "The 126ers", "Youtube Audio Library Rock 2", "Rock", 160,
                "http://audiobookpreviews.kilatstorage.com/2929_20180321053709.mp3",
                "http://audiobookchaptercoverarts.kilatstorage.com/6696_20180417152054.png",
                "album_youtube_audio_library_rock_2");
        if(mPlaybackControls.getVisibility() == View.VISIBLE){
            mPlaybackControls.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Intent k = new Intent(MainActivity.this, FullscreenActivity.class);
                        startActivity(k);
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        Button button = (Button) findViewById(R.id.btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaBrowserFragment fragment = (MediaBrowserFragment) getFragmentManager().findFragmentByTag(MediaBrowserFragment.class.getName());

                if (fragment == null) {
                    fragment = new MediaBrowserFragment();
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.container, fragment);

                    // If this is not the top level media (root), we add it to the fragment back stack,
                    // so that actionbar toggle and Back will work appropriately:
                    transaction.commit();

                }
            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();
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
        mMediaBrowser.disconnect();
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
            return MediaItemViewHolder.setupView((Activity) getContext(), convertView, parent, item.getDescription(), itemState);
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
                controls.playFromMediaId(mCurrentMetadata.getDescription().getMediaId(), null);

            } else {
                controls.pause();
            }
        }
    };

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
        MediaControllerCompat.setMediaController(this, mediaController);


    }
}
