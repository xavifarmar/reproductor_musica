package com.example.spotify;

public class Song {

    private String name;
    private String path;
    private String coverPath;  // Campo para la ruta de la portada

    // Constructor
    public Song(String name, String path, String coverPath) {
        this.name = name;
        this.path = path;
        this.coverPath = coverPath;
    }

    // Métodos getter para obtener los valores de name, path y coverPath
    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public String getCoverPath() {
        return coverPath;
    }

    // Opcional: Método toString() para facilitar la visualización de los objetos
    @Override
    public String toString() {
        return "Song{name='" + name + "', path='" + path + "', coverPath='" + coverPath + "'}";
    }
}
