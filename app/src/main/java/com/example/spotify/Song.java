package com.example.spotify;

public class Song {

    private String name;
    private String path;

    // Constructor
    public Song(String name, String path) {
        this.name = name;
        this.path = path;
    }

    // Métodos getter para obtener los valores de name y path
    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    // Opcional: Puedes agregar un método toString() para facilitar la visualización de los objetos
    @Override
    public String toString() {
        return "Song{name='" + name + "', path='" + path + "'}";
    }


}
