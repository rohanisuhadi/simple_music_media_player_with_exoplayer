/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.kamtiz.musicplayersample;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class MediaBrowserFragment extends Fragment {

    private static final String TAG = LogHelper.makeLogTag(MediaBrowserFragment.class);

    private static final String ARG_MEDIA_ID = "media_id";

    private Activity activity;
    private Context context;
    private BrowseAdapterBaru mBrowserAdapter;

    private MediaMetadataCompat mCurrentMetadata;
    private PlaybackStateCompat mCurrentState;
    private MediaBrowserCompat mMediaBrowser;

    private MusicLibrary musicLibrary;

    private MediaBrowserCompat mediaBrowserFragment;
    private MediaMetadataCompat mCurrentMetadataFragment;
    private PlaybackStateCompat mCurrentStateFragment;

    private MediaFragmentListener mMediaFragmentListener;

    private final BroadcastReceiver mConnectivityChangeReceiver = new BroadcastReceiver() {
        private boolean oldOnline = false;
        @Override
        public void onReceive(Context context, Intent intent) {
            // We don't care about network changes while this fragment is not associated
            // with a media ID (for example, while it is being initialized)
                boolean isOnline = NetworkHelper.isOnline(context);
                if (isOnline != oldOnline) {
                    oldOnline = isOnline;
//                    checkForUserVisibleErrors(false);
                    if (isOnline) {
                        mBrowserAdapter.notifyDataSetChanged();
                    }
                }

        }
    };

    public MediaBrowserFragment() {

    }

    @SuppressLint("ValidFragment")
    public MediaBrowserFragment(Activity activity, Context context) {
        this.activity = activity;
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LogHelper.d(TAG, "fragment.onCreateView");
        View rootView = inflater.inflate(R.layout.fragment_list, container, false);

        mBrowserAdapter = new BrowseAdapterBaru(getActivity());
        ListView listView = (ListView) rootView.findViewById(R.id.list_view);
        listView.setAdapter(mBrowserAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MediaBrowserCompat.MediaItem item = mBrowserAdapter.getItem(position);
                mMediaFragmentListener.onMediaItemSelected(item);
            }
        });

        musicLibrary = new MusicLibrary();
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

        return rootView;
    }

    // An adapter for showing the list of browsed MediaItem's
    private class BrowseAdapterBaru extends ArrayAdapter<MediaBrowserCompat.MediaItem> {
        public BrowseAdapterBaru(Activity context) {
            super(context, R.layout.media_list_item, new ArrayList<MediaBrowserCompat.MediaItem>());
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            MediaBrowserCompat.MediaItem item = getItem(position);
            int itemState = MediaItemViewHolder.STATE_NONE;
            if (item.isPlayable()) {
                String itemMediaId = item.getDescription().getMediaId();
                int playbackState = PlaybackStateCompat.STATE_NONE;
                if (mCurrentStateFragment != null) {
                    playbackState = mCurrentStateFragment.getState();
                }
                if (mCurrentMetadataFragment != null &&
                        itemMediaId.equals(mCurrentMetadataFragment.getDescription().getMediaId())) {
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

    // Receive callbacks from the MediaController. Here we update our state such as which queue
    // is being shown, the current title and description and the PlaybackState.
    private final MediaControllerCompat.Callback mMediaControllerCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
//            updateMetadata(metadata);
            if (metadata == null) {
                return;
            }
            mBrowserAdapter.notifyDataSetChanged();
        }

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
//            updatePlaybackState(state);
            mBrowserAdapter.notifyDataSetChanged();
        }

        @Override
        public void onSessionDestroyed() {
//            updatePlaybackState(null);
            mBrowserAdapter.notifyDataSetChanged();
        }
    };


    private final MediaBrowserCompat.SubscriptionCallback mSubscriptionCallback =
            new MediaBrowserCompat.SubscriptionCallback() {
                @Override
                public void onChildrenLoaded(@NonNull String parentId,@NonNull List<MediaBrowserCompat.MediaItem> children) {
                    try {
                        mBrowserAdapter.clear();
                        for (MediaBrowserCompat.MediaItem item : children) {
                            Log.e("Masukdata","bener");
                            mBrowserAdapter.add(item);
                        }
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




    @Override
    public void onStart() {
        super.onStart();
        mediaBrowserFragment = mMediaFragmentListener.getMediaBrowser();
        mCurrentMetadataFragment = mMediaFragmentListener.getMediaMetadataCompat();
        mCurrentStateFragment = mMediaFragmentListener.getPlaybackStateCompat();

        MediaBrowserCompat mediaBrowser = mMediaFragmentListener.getMediaBrowser();


        if (mediaBrowser.isConnected()) {
            onConnected();
        }

        this.getActivity().registerReceiver(mConnectivityChangeReceiver,
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

    }

    public void onConnected() {
        if (isDetached()) {
            return;
        }

        // Unsubscribing before subscribing is required if this mediaId already has a subscriber
        // on this MediaBrowser instance. Subscribing to an already subscribed mediaId will replace
        // the callback, but won't trigger the initial callback.onChildrenLoaded.
        //
        // This is temporary: A bug is being fixed that will make subscribe
        // consistently call onChildrenLoaded initially, no matter if it is replacing an existing
        // subscriber or not. Currently this only happens if the mediaID has no previous
        // subscriber or if the media content changes on the service side, so we need to
        // unsubscribe first.

        mMediaFragmentListener.getMediaBrowser().unsubscribe(mMediaFragmentListener.getMediaBrowser().getRoot());

        mMediaFragmentListener.getMediaBrowser().subscribe(mMediaFragmentListener.getMediaBrowser().getRoot(), mSubscriptionCallback);

        // Add MediaController callback so we can redraw the list when metadata changes:
        MediaControllerCompat controller = MediaControllerCompat.getMediaController(getActivity());
        if (controller != null) {
            controller.registerCallback(mMediaControllerCallback);
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mediaBrowserFragment != null && mediaBrowserFragment.isConnected()) {
            String data = mCurrentMetadataFragment.getDescription().getMediaId();
            if(data==null){
                data = mediaBrowserFragment.getRoot();
            }
            mediaBrowserFragment.unsubscribe(data);
        }
        mediaBrowserFragment.disconnect();
        MediaControllerCompat controller = MediaControllerCompat.getMediaController(getActivity());
        if (controller != null) {
            controller.unregisterCallback(mMediaControllerCallback);
        }

        this.getActivity().unregisterReceiver(mConnectivityChangeReceiver);

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mMediaFragmentListener = null;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // If used on an activity that doesn't implement MediaFragmentListener, it
        // will throw an exception as expected:
        mMediaFragmentListener = (MediaFragmentListener) activity;
    }

    public interface MediaFragmentListener extends MediaBrowserProvider {
        void onMediaItemSelected(MediaBrowserCompat.MediaItem item);
        MediaMetadataCompat getMediaMetadataCompat();
        PlaybackStateCompat getPlaybackStateCompat();
    }

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
        MediaControllerCompat mediaController = new MediaControllerCompat(context, token);
        Log.e("Berjalan","Datang");
        updatePlaybackState(mediaController.getPlaybackState());
        updateMetadata(mediaController.getMetadata());
        mediaController.registerCallback(mMediaControllerCallback);
        MediaControllerCompat.setMediaController(getActivity(), mediaController);
    }

    private void updatePlaybackState(PlaybackStateCompat state) {
        mCurrentState = state;
    }

    private void updateMetadata(MediaMetadataCompat metadata) {
        mCurrentMetadata = metadata;
        mBrowserAdapter.notifyDataSetChanged();
    }

}
