package com.example.spotify;

import android.graphics.Bitmap;

public class Song {
    private String name;
    private String path;
    private Bitmap coverBitmap;

    // Constructor actualizado para aceptar Bitmap para la portada
    public Song(String name, String path, Bitmap coverBitmap) {
        this.name = name;
        this.path = path;
        this.coverBitmap = coverBitmap;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public Bitmap getCoverBitmap() {
        return coverBitmap;
    }
}
