package com.example.spotify;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spotify.R;
import com.example.spotify.Song;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.SongViewHolder> {

    private Context context;
    private List<Song> songs;
    private OnItemClickListener onItemClickListener;

    // Constructor actualizado para aceptar un listener
    public SongsAdapter(Context context, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.songs = new ArrayList<>();
        this.onItemClickListener = onItemClickListener;  // Inicializar el listener
        obtenerCanciones();  // Cargar las canciones al inicializar el adaptador
    }

    // ViewHolder para los elementos del RecyclerView
    public static class SongViewHolder extends RecyclerView.ViewHolder {
        TextView songName;
        ImageView coverImageView;

        public SongViewHolder(View itemView) {
            super(itemView);
            songName = itemView.findViewById(R.id.text1);  // Usamos el ID correcto para el TextView
            coverImageView = itemView.findViewById(R.id.coverImageView);  // Usamos el ID correcto para la portada
        }
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflamos el layout personalizado
        View view = LayoutInflater.from(context).inflate(R.layout.simple_list_item_1, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songs.get(position);
        holder.songName.setText(song.getName());  // Asignamos el nombre de la canción al TextView

        // Obtener la portada de la canción
        loadCoverImage(holder.coverImageView, song.getCoverBitmap());

        // Manejo del clic en el ítem
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(song);  // Llamamos al listener
            }
        });
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    // Método para obtener las canciones de la carpeta "Downloads"
    private void obtenerCanciones() {
        Log.d("SongsAdapter", "Iniciando la consulta para obtener canciones...");
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {
                MediaStore.Audio.Media.DISPLAY_NAME,  // Nombre del archivo
                MediaStore.Audio.Media.DATA,           // Ruta del archivo
                MediaStore.Audio.Media.ALBUM_ID       // ID del álbum para la portada
        };

        // Filtra para obtener solo archivos dentro de la carpeta "Download"
        String selection = MediaStore.Audio.Media.DATA + " LIKE ?";  // Filtramos por la carpeta "Download"
        String[] selectionArgs = new String[]{"%/Download/%"};

        try {
            Log.d("SongsAdapter", "Ejecutando consulta a MediaStore...");
            Cursor cursor = contentResolver.query(uri, projection, selection, selectionArgs, null);
            if (cursor != null) {
                Log.d("SongsAdapter", "Consulta exitosa, procesando resultados...");
                int nameColumnIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME);
                int pathColumnIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
                int albumIdColumnIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);

                // Verificar si los índices de las columnas son válidos (mayores o iguales a 0)
                if (nameColumnIndex >= 0 && pathColumnIndex >= 0 && albumIdColumnIndex >= 0) {
                    int count = 0;  // Contador para verificar el número de canciones cargadas
                    while (cursor.moveToNext()) {
                        String name = cursor.getString(nameColumnIndex);
                        String path = cursor.getString(pathColumnIndex);
                        long albumId = cursor.getLong(albumIdColumnIndex);
                        Log.d("SongsAdapter", "AlbumId: " + albumId);

                        // Log para verificar las rutas encontradas
                        Log.d("SongPath", "Nombre: " + name + ", Ruta: " + path + ", AlbumId: " + albumId);

                        // Verificamos si el path no es null antes de añadir la canción
                        if (path != null) {
                            Bitmap coverBitmap = getAlbumCoverBitmap(albumId); // Obtenemos el Bitmap de la portada
                            songs.add(new Song(name, path, coverBitmap));  // Agregar la canción
                            count++;  // Incrementamos el contador para verificar
                        }
                    }

                    // Verificar cuántas canciones se cargaron
                    Log.d("SongPath", "Total de canciones cargadas: " + count);
                } else {
                    Log.e("SongsAdapter", "Las columnas no son válidas, la consulta no contiene los datos esperados.");
                }
                cursor.close();  // Cerrar el cursor después de usarlo
            } else {
                Log.e("SongsAdapter", "No se encontró ningún cursor o canciones.");
                Toast.makeText(context, "No se encontraron canciones", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("SongsAdapter", "Error al consultar la base de datos", e);
            Toast.makeText(context, "Ocurrió un error al obtener las canciones", Toast.LENGTH_SHORT).show();
        }

        // Asegurarse de que notifyDataSetChanged() se llama para actualizar la interfaz de usuario
        Log.d("SongsAdapter", "Notificando cambios en el adaptador...");
        notifyDataSetChanged();
    }

    // Método corregido para cargar la portada desde un Bitmap
    private void loadCoverImage(ImageView imageView, Bitmap coverBitmap) {
        Log.d("SongsAdapter", "Intentando cargar la portada de la canción...");

        if (coverBitmap != null) {
            // Si el Bitmap es válido, lo mostramos en el ImageView
            imageView.setImageBitmap(coverBitmap);
            Log.d("SongsAdapter", "Portada cargada correctamente.");
        } else {
            // Si no se puede obtener el Bitmap, mostramos la imagen predeterminada
            imageView.setImageResource(R.drawable.default_cover);
            Log.d("SongsAdapter", "Portada no encontrada, usando imagen predeterminada.");
        }
    }

    // Método corregido para obtener la portada del álbum
    private Bitmap getAlbumCoverBitmap(long albumId) {
        Bitmap coverBitmap = null;
        // Formamos la URI del álbum usando el albumId
        Uri albumUri = ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, albumId);

        // Consultamos la URI para obtener la ruta de la portada del álbum
        Cursor cursor = context.getContentResolver().query(
                albumUri,
                new String[]{MediaStore.Audio.Albums.ALBUM_ART},
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            String albumArtPath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
            cursor.close();

            if (albumArtPath != null && !albumArtPath.isEmpty()) {
                // Si la ruta de la portada es válida, la decodificamos en un Bitmap
                coverBitmap = BitmapFactory.decodeFile(albumArtPath);
            }
        }

        // Si no encontramos la portada en la URI del álbum, intentamos obtenerla de los metadatos
        if (coverBitmap == null) {
            coverBitmap = getSongCoverArt(albumId); // Intentamos obtenerla desde los metadatos
        }

        // Si no encontramos ninguna portada, devolvemos una imagen predeterminada
        if (coverBitmap == null) {
            coverBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_cover);
        }

        return coverBitmap;
    }

    // Método para obtener la portada de la canción desde los metadatos
    private Bitmap getSongCoverArt(long albumId) {
        Bitmap coverBitmap = null;
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            String songPath = getSongPathByAlbumId(albumId);  // Obtén la ruta de la canción

            if (songPath != null) {
                retriever.setDataSource(songPath);
                byte[] art = retriever.getEmbeddedPicture();

                if (art != null) {
                    coverBitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
                }
            }

        } catch (Exception e) {
            Log.e("SongsAdapter", "Error al obtener la portada de los metadatos", e);
        }
        return coverBitmap;
    }

    // Método para obtener la ruta de la canción por su ID de álbum
    private String getSongPathByAlbumId(long albumId) {
        String path = null;
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.ALBUM_ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(albumId)};

        Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Audio.Media.DATA}, selection, selectionArgs, null);

        if (cursor != null && cursor.moveToFirst()) {
            path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            cursor.close();
        }
        return path;
    }

    // Interfaz para manejar los clics en las canciones
    public interface OnItemClickListener {
        void onItemClick(Song song);
    }
}
