package com.example.spotify;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.SongViewHolder> {

    private Context context;
    private List<Song> songs;

    public SongsAdapter(Context context) {
        this.context = context;
        this.songs = new ArrayList<>();
        obtenerCanciones();  // Cargar las canciones al inicializar el adaptador
    }

    // ViewHolder para los elementos del RecyclerView
    public static class SongViewHolder extends RecyclerView.ViewHolder {
        TextView songName;

        public SongViewHolder(View itemView) {
            super(itemView);
            songName = itemView.findViewById(android.R.id.text1);  // Usamos un TextView básico
        }
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songs.get(position);
        holder.songName.setText(song.getName());  // Asignamos el nombre de la canción al TextView
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    // Método para obtener las canciones de la carpeta "Downloads"
    private void obtenerCanciones() {
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {
                MediaStore.Audio.Media.DISPLAY_NAME,  // Nombre del archivo
                MediaStore.Audio.Media.DATA           // Ruta del archivo
        };

        String selection = MediaStore.Audio.Media.DATA + " LIKE ?";
        String[] selectionArgs = new String[]{"%/Download/%"};
        try:
        Cursor cursor = contentResolver.query(uri, projection, selection, selectionArgs, null);
        if (cursor != null) {
            int nameColumnIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME);
            int pathColumnIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);

            //Verificamos si las columnas existen (Debe ser mayor de 0)
            if (nameColumnIndex >= 0 && pathColumnIndex >= 0) {

                if (cursor.moveToFirst()) {
                    do {
                        String name = cursor.getString(nameColumnIndex);
                        String path = cursor.getString(pathColumnIndex);

                        // Solo añadimos canciones que están en la carpeta /Download/
                        if (path != null && path.contains("/Download/")) {
                            songs.add(new Song(name, path));
                        }
                    } while (cursor.moveToNext());
                }
                cursor.close();  // Cerrar el cursor
            } else {
                // Si alguna columna no existe, mostramos un mensaje de error
                Log.e("SongsAdapter", "Columnas de datos no encontradas.");
                Toast.makeText(context, "Error al obtener las canciones", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "No se encontraron canciones en Downloads", Toast.LENGTH_SHORT).show();
        }
        notifyDataSetChanged();  // Notificar que los datos han cambiado
    }
}
