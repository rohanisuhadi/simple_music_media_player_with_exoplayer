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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

class MusicLibrary {

    private static final TreeMap<String, MediaMetadataCompat> music = new TreeMap<>();
    private static final HashMap<String, String> albumRes = new HashMap<>();
    private static final HashMap<String, String> musicRes = new HashMap<String, String>();
    static {
        createMediaMetadata("Jazz_In_Paris", "Jazz in Paris",
                "Media Right Productions", "Jazz & Blues", "Jazz", 103,
                "http://audiobookpreviews.kilatstorage.com/2989_20180417144035.mp3",
                "http://audiobookchaptercoverarts.kilatstorage.com/6696_20180417152054.png",
                "album_jazz_blues");
        createMediaMetadata("The_Coldest_Shoulder",
                "The Coldest Shoulder", "The 126ers", "Youtube Audio Library Rock 2", "Rock", 160,
                "http://audiobookpreviews.kilatstorage.com/2929_20180321053709.mp3",
                "http://audiobookchaptercoverarts.kilatstorage.com/6696_20180417152054.png",
                "album_youtube_audio_library_rock_2");
    }

    public static String getRoot() {
        return "media_id";
    }

    public static String getSongUri(String mediaId) {
        return getMusicRes(mediaId);
    }

    private static String getAlbumArtUri(String albumArtResName) {
        return albumArtResName;
    }

    private static String getMusicRes(String mediaId) {
        return String.valueOf(musicRes.containsKey(mediaId) ? musicRes.get(mediaId) : 0);
    }

    private static String getAlbumRes(String mediaId) {
        return albumRes.containsKey(mediaId) ? albumRes.get(mediaId) : "";
    }

    public static String getAlbumBitmap(Context ctx, String mediaId) {

//        return Glide.with(ctx).load(MusicLibrary.getAlbumRes(mediaId));
//        return BitmapFactory.decodeResource(ctx.getResources(), MusicLibrary.getAlbumRes(mediaId));
        return MusicLibrary.getAlbumRes(mediaId);
    }

    public static List<MediaBrowserCompat.MediaItem> getMediaItems() {
        List<MediaBrowserCompat.MediaItem> result = new ArrayList<>();
        for (MediaMetadataCompat metadata: music.values()) {
            result.add(new MediaBrowserCompat.MediaItem(metadata.getDescription(),
                    MediaBrowserCompat.MediaItem.FLAG_PLAYABLE));
        }
        return result;
    }

    public static String getPreviousSong(String currentMediaId) {
        String prevMediaId = music.lowerKey(currentMediaId);
        if (prevMediaId == null) {
            prevMediaId = music.firstKey();
        }
        return prevMediaId;
    }

    public static String getNextSong(String currentMediaId) {
        String nextMediaId = music.higherKey(currentMediaId);
        if (nextMediaId == null) {
            nextMediaId = music.firstKey();
        }
        return nextMediaId;
    }

    public static MediaMetadataCompat getMetadata(Context ctx, String mediaId) {
        MediaMetadataCompat metadataWithoutBitmap = music.get(mediaId);
        String albumArt = getAlbumBitmap(ctx, mediaId);

        // Since MediaMetadata is immutable, we need to create a copy to set the album art
        // We don't set it initially on all items so that they don't take unnecessary memory
        MediaMetadataCompat.Builder builder = new MediaMetadataCompat.Builder();
        for (String key: new String[]{MediaMetadataCompat.METADATA_KEY_MEDIA_ID,
                MediaMetadataCompat.METADATA_KEY_ALBUM, MediaMetadataCompat.METADATA_KEY_ARTIST,
                MediaMetadataCompat.METADATA_KEY_GENRE, MediaMetadataCompat.METADATA_KEY_TITLE}) {
            builder.putString(key, metadataWithoutBitmap.getString(key));
        }
        builder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION,
                metadataWithoutBitmap.getLong(MediaMetadataCompat.METADATA_KEY_DURATION));
        builder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, albumArt);
        return builder.build();
    }

    private static void createMediaMetadata(String mediaId, String title, String artist,
                                            String album, String genre, long duration, String musicResId, String albumArtResId,
                                            String albumArtResName) {
        music.put(mediaId,
                new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, mediaId)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration * 1000)
                .putString(MediaMetadataCompat.METADATA_KEY_GENRE, genre)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, getAlbumArtUri(albumArtResName))
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, getAlbumArtUri(albumArtResName))
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .build());
        albumRes.put(mediaId, albumArtResId);
        musicRes.put(mediaId, musicResId);
    }

}