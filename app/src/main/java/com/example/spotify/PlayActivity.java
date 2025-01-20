package com.example.spotify;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class PlayActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private ImageButton playButton, pauseButton, stopButton;
    private SeekBar seekBar;
    private ImageView coverImageView;  // ImageView for displaying the cover image
    private boolean isPaused = false;  // State to track if it's paused
    private Handler handler = new Handler();  // Handler to update the SeekBar

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        // Initialize buttons and ImageView for the cover
        playButton = findViewById(R.id.ic_play);  // Play button
        pauseButton = findViewById(R.id.ic_pause);  // Pause button
        seekBar = findViewById(R.id.seekBar);  // Initialize SeekBar
        //coverImageView = findViewById(R.id.song);  // ImageView for the song cover

        // Retrieve song data from the Intent
        Intent intent = getIntent();
        String songPath = intent.getStringExtra("song_path");
        String songName = intent.getStringExtra("song_name");
        String coverPath = intent.getStringExtra("cover_path");  // Get the cover path

        // Set up the media player and start playing the song
        if (songPath != null) {
            try {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(songPath);
                mediaPlayer.prepare();  // Prepare the MediaPlayer
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        Toast.makeText(PlayActivity.this, "Now Playing: " + songName, Toast.LENGTH_SHORT).show();
                        seekBar.setMax(mediaPlayer.getDuration());  // Set the SeekBar's maximum value to the song's duration
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error loading the song", Toast.LENGTH_SHORT).show();
            }
        }

        // Set the cover image for the song
        if (coverPath != null && !coverPath.isEmpty()) {
            // Load the image from the coverPath (local file)
            loadCoverImage(coverPath);
        } else {
            // Set a default cover image if no coverPath is provided
            coverImageView.setImageResource(R.drawable.default_cover);
        }

        // Play button action
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer != null) {
                    if (!mediaPlayer.isPlaying()) {
                        // If the music is not playing
                        if (isPaused) {
                            mediaPlayer.start();  // Resume playback
                            isPaused = false;
                            updatePlayPauseButtons();  // Update buttons
                            updateSeekBar();  // Start updating the SeekBar
                            Toast.makeText(PlayActivity.this, "Playing music", Toast.LENGTH_SHORT).show();
                        } else {
                            // If it's the first time playing, start from the beginning
                            mediaPlayer.seekTo(0);
                            mediaPlayer.start();
                            updatePlayPauseButtons();  // Update buttons
                            updateSeekBar();  // Start updating the SeekBar
                            Toast.makeText(PlayActivity.this, "Playing music", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });

        // Pause button action
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    isPaused = true;
                    updatePlayPauseButtons();  // Update buttons
                    Toast.makeText(PlayActivity.this, "Music paused", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Iniciar la actualización del SeekBar
        updateSeekBar();
    }

    private void updatePlayPauseButtons() {
        // Change visibility between Play and Pause buttons
        if (isPaused) {
            // If paused, show the Play button and hide the Pause button
            playButton.setVisibility(View.VISIBLE);
            pauseButton.setVisibility(View.GONE);
        } else {
            // If playing, show the Pause button and hide the Play button
            playButton.setVisibility(View.GONE);
            pauseButton.setVisibility(View.VISIBLE);
        }
    }

    private void updateSeekBar() {
        // Update SeekBar every second while the music is playing
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    int currentPosition = mediaPlayer.getCurrentPosition();  // Get the current position
                    seekBar.setProgress(currentPosition);  // Update the SeekBar
                    handler.postDelayed(this, 1000);  // Re-run this Runnable every second
                }
            }
        };
        handler.post(runnable);  // Start the SeekBar update process
    }

    private void loadCoverImage(String coverPath) {
        // Load the cover image from the file system
        File coverFile = new File(coverPath);
        if (coverFile.exists()) {
            try {
                FileInputStream fis = new FileInputStream(coverFile);
                Bitmap bitmap = BitmapFactory.decodeStream(fis);  // Decode the image
                coverImageView.setImageBitmap(bitmap);  // Set the bitmap to the ImageView
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
                coverImageView.setImageResource(R.drawable.default_cover);  // Set default if error occurs
            }
        } else {
            coverImageView.setImageResource(R.drawable.default_cover);  // Set default if file does not exist
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }
}
