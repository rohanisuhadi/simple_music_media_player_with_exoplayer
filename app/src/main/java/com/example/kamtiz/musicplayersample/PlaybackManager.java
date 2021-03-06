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

import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;
import android.os.SystemClock;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;



class PlaybackManager implements AudioManager.OnAudioFocusChangeListener{

    private static final String TAG = LogHelper.makeLogTag(PlaybackManager.class);

    public static final int FAST_REWIND = 15000;
    public static final int FAST_FORWARD = 15000;

    private final Context mContext;
    private int mState;
    private boolean mPlayOnFocusGain;
    private volatile MediaMetadataCompat mCurrentMedia;

    private SimpleExoPlayer mMediaPlayer;

    private final Callback mCallback;
    private final AudioManager mAudioManager;
    private boolean mExoPlayerNullIsStopped =  false;

    private final ExoPlayerEventListener mEventListener = new ExoPlayerEventListener();
    private float playBackSpeed = 1.0f;

    public PlaybackManager(Context context, Callback callback) {
        this.mContext = context;
        this.mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        this.mCallback = callback;
    }

    public boolean isPlaying() {
        return mPlayOnFocusGain || (mMediaPlayer != null && mMediaPlayer.getPlayWhenReady());
    }

    public MediaMetadataCompat getCurrentMedia() {
        return mCurrentMedia;
    }

    public String getCurrentMediaId() {
        return mCurrentMedia == null ? null : mCurrentMedia.getDescription().getMediaId();
    }

    public long getCurrentStreamPosition() {
        return mMediaPlayer != null ? mMediaPlayer.getCurrentPosition() : 0;
    }

    public void play(MediaMetadataCompat metadata) {
        String mediaId = metadata.getDescription().getMediaId();
        boolean mediaChanged = (mCurrentMedia == null || !getCurrentMediaId().equals(mediaId));

        if (mMediaPlayer == null) {

            mMediaPlayer = ExoPlayerFactory.newSimpleInstance(
                    new DefaultRenderersFactory(mContext),
                    new DefaultTrackSelector(),
                    new DefaultLoadControl());
            mMediaPlayer.addListener(mEventListener);

            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//            mMediaPlayer.setWakeMode(mContext.getApplicationContext(),
//                    PowerManager.PARTIAL_WAKE_LOCK);
        }
//        else {
//            if (mediaChanged) {
////                mMediaPlayer.release();
////                releaseResources(true);
//            }
//        }

        if (mediaChanged) {
            mCurrentMedia = metadata;
            try {
                Log.e("URL",MusicLibrary.getSongUri(mediaId));

                // Produces DataSource instances through which media data is loaded.
                DataSource.Factory dataSourceFactory =
                        new DefaultDataSourceFactory(
                                mContext, Util.getUserAgent(mContext, "uamp"), null);
                // Produces Extractor instances for parsing the media data.
                ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
                // The MediaSource represents the media to be played.
                ExtractorMediaSource.Factory extractorMediaFactory =
                        new ExtractorMediaSource.Factory(dataSourceFactory);
                extractorMediaFactory.setExtractorsFactory(extractorsFactory);
                MediaSource mediaSource =
                        extractorMediaFactory.createMediaSource(Uri.parse(MusicLibrary.getSongUri(mediaId)));

                // Prepares media to play (happens on background thread) and triggers
                // {@code onPlayerStateChanged} callback when the stream is ready to play.
                mMediaPlayer.prepare(mediaSource);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        if (tryToGetAudioFocus()) {
            mPlayOnFocusGain = false;
            mMediaPlayer.setPlayWhenReady(true);
            mState = PlaybackStateCompat.STATE_PLAYING;
            updatePlaybackState();
        } else {
            mPlayOnFocusGain = true;
        }
    }

    private void releaseResources(boolean releasePlayer) {

        // Stops and releases player (if requested and available).
        if (releasePlayer && mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer.removeListener(mEventListener);
            mMediaPlayer = null;
//            mExoPlayerNullIsStopped = true;
            mPlayOnFocusGain = false;
        }

    }

    public void pause() {
        if (isPlaying()) {
            mMediaPlayer.setPlayWhenReady(false);
            mAudioManager.abandonAudioFocus(this);
        }
        mState = PlaybackStateCompat.STATE_PAUSED;
        updatePlaybackState();
    }

    public void seekTo(long position) {
        LogHelper.e(TAG, "onSeekTo:", position);
        if (mMediaPlayer != null) {
            LogHelper.e(TAG, "onSeekToMasuk:", position);
            mMediaPlayer.seekTo(position);
            updatePlaybackState();
        }
    }

    public void setPlaybackSpeedPlus(){
        if(mMediaPlayer!=null){
            playBackSpeed = (float) (playBackSpeed + 0.1);
            PlaybackParameters playbackParameters = new PlaybackParameters(playBackSpeed, 1f);
            mMediaPlayer.setPlaybackParameters(playbackParameters);
            updatePlaybackState();
        }
    }

    public void setPlaybackSpeedMinus(){
        if(mMediaPlayer!=null){
            playBackSpeed = (float) (playBackSpeed - 0.1);
            PlaybackParameters playbackParameters = new PlaybackParameters(playBackSpeed, 1f);
            mMediaPlayer.setPlaybackParameters(playbackParameters);
            updatePlaybackState();
        }
    }

    public void setPlaybackSpeedDefault(){
        if(mMediaPlayer!=null){
            playBackSpeed = 1.0f;
            PlaybackParameters playbackParameters = new PlaybackParameters(playBackSpeed, 1f);
            mMediaPlayer.setPlaybackParameters(playbackParameters);
            updatePlaybackState();
        }
    }

    public long getDuration(){
        if(mMediaPlayer!=null)
            return mMediaPlayer.getDuration();
        else return 0;
    }

    public void stop() {
        mState = PlaybackStateCompat.STATE_STOPPED;
        updatePlaybackState();
        // Give up Audio focus
        mAudioManager.abandonAudioFocus(this);
        // Relax all resources
        releaseMediaPlayer();
    }

    /**
     * Try to get the system audio focus.
     */
    private boolean tryToGetAudioFocus() {
        int result = mAudioManager.requestAudioFocus(
                this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    /**
     * Called by AudioManager on audio focus changes.
     * Implementation of {@link AudioManager.OnAudioFocusChangeListener}
     */
    @Override
    public void onAudioFocusChange(int focusChange) {
        boolean gotFullFocus = false;
        boolean canDuck = false;

        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                gotFullFocus = true;
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Audio focus was lost, but it's possible to duck (i.e.: play quietly)
                canDuck = true;
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost audio focus, but will gain it back (shortly), so note whether
                // playback should resume
                canDuck = focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK;
                mPlayOnFocusGain = mMediaPlayer != null && mMediaPlayer.getPlayWhenReady();
                if (mPlayOnFocusGain ){
                    Log.e("STATUS","TRUE AUDIOFOCUS_LOSS_TRANSIENT");
                }else {
                    Log.e("STATUS","FALSE AUDIOFOCUS_LOSS_TRANSIENT");
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost audio focus, probably "permanently"
                canDuck = focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK;
                break;
        }


        if (gotFullFocus || canDuck) {
            Log.e("STATUS","TRUE gotFullFocus || canDuck");
            if (mMediaPlayer != null) {
                if (mPlayOnFocusGain) {
                    Log.e("STATUS","TRUE gotFullFocus || canDuck");
                    mPlayOnFocusGain = false;
                    mMediaPlayer.setPlayWhenReady(true);
                    mState = PlaybackStateCompat.STATE_PLAYING;
                    updatePlaybackState();
                }

            }
        } else if (mState == PlaybackStateCompat.STATE_PLAYING) {
            Log.e("STATUS","TRUE mState == PlaybackStateCompat.STATE_PLAYING");
            mMediaPlayer.setPlayWhenReady(false);
            mState = PlaybackStateCompat.STATE_PAUSED;
            updatePlaybackState();
        }


    }

    /**
     * Releases resources used by the service for playback.
     */
    private void releaseMediaPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.removeListener(mEventListener);
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    private long getAvailableActions() {
        long actions = PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID |
                PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH |
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT  | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS;
        if (isPlaying()) {
            actions |= PlaybackStateCompat.ACTION_PAUSE;
        }
        return actions;
    }

    private void updatePlaybackState() {
        if (mCallback == null) {
            return;
        }
        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(getAvailableActions());

        stateBuilder.setState(mState, getCurrentStreamPosition(), playBackSpeed, SystemClock.elapsedRealtime());
        mCallback.onPlaybackStatusChanged(stateBuilder.build());
    }

    public interface Callback {
        void onPlaybackStatusChanged(PlaybackStateCompat state);
        void onCompletion();
    }


    private final class ExoPlayerEventListener implements Player.EventListener {
        @Override
        public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {
            // Nothing to do.
        }

        @Override
        public void onTracksChanged(
                TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
            // Nothing to do.
        }

        @Override
        public void onLoadingChanged(boolean isLoading) {
            // Nothing to do.
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            switch (playbackState) {
                case Player.STATE_IDLE:
                case Player.STATE_BUFFERING:
                case Player.STATE_READY:
//                    if (mCallback != null) {
//                        mCallback.onPlaybackStatusChanged(getState());
//                    }
                    break;
                case Player.STATE_ENDED:

                    // The media player finished playing the current song.
                    if (mCallback != null) {
                        Log.e("Selesai","TEst");
                        mCallback.onCompletion();

                    }
                    break;
            }
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            final String what;
            switch (error.type) {
                case ExoPlaybackException.TYPE_SOURCE:
                    what = error.getSourceException().getMessage();
                    break;
                case ExoPlaybackException.TYPE_RENDERER:
                    what = error.getRendererException().getMessage();
                    break;
                case ExoPlaybackException.TYPE_UNEXPECTED:
                    what = error.getUnexpectedException().getMessage();
                    break;
                default:
                    what = "Unknown: " + error;
            }

            Log.e("CLASS PLAYBACK", "ExoPlayer error: what=" + what);
            if (mCallback != null) {
//                mCallback.onError("ExoPlayer error " + what);
            }
        }

        @Override
        public void onPositionDiscontinuity(int reason) {
            // Nothing to do.
        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
            // Nothing to do.
        }

        @Override
        public void onSeekProcessed() {
            // Nothing to do.
        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {
            // Nothing to do.
        }

        @Override
        public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
            // Nothing to do.
        }
    }

}
