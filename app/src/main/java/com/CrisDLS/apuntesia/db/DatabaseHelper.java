package com.CrisDLS.apuntesia.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import android.database.Cursor;

import com.CrisDLS.apuntesia.models.Apunte;
import com.CrisDLS.apuntesia.models.Materia;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Información de la Base de Datos
    private static final String DATABASE_NAME = "ApuntesIA.db";
    private static final int DATABASE_VERSION = 3;

    // Tabla: MATERIAS
    private static final String TABLE_MATERIAS = "materias";
    private static final String COL_MATERIA_ID = "id";
    private static final String COL_MATERIA_NOMBRE = "nombre";
    private static final String COL_MATERIA_NOTION_ID = "notion_id";

    // Tabla: APUNTES
    private static final String TABLE_APUNTES = "apuntes";
    private static final String COL_APUNTE_ID = "id";
    private static final String COL_APUNTE_MATERIA_ID = "materia_id";
    private static final String COL_APUNTE_FECHA = "fecha";
    private static final String COL_APUNTE_TITULO = "titulo";
    private static final String COL_APUNTE_RESUMEN = "resumen";
    private static final String COL_APUNTE_RUTA_AUDIO = "ruta_audio";
    private static final String COL_APUNTE_TEXTO = "texto_resumen";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Habilitar el soporte para Llaves Foráneas (Foreign Keys)
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Crear tabla materias
        String CREATE_MATERIAS_TABLE = "CREATE TABLE " + TABLE_MATERIAS + " ("
                + COL_MATERIA_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_MATERIA_NOMBRE + " TEXT NOT NULL, "
                + COL_MATERIA_NOTION_ID + " TEXT"
                + ")";
        db.execSQL(CREATE_MATERIAS_TABLE);

        // Crear tabla apuntes vinculada a materias
        String CREATE_APUNTES_TABLE = "CREATE TABLE " + TABLE_APUNTES + " ("
                + COL_APUNTE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_APUNTE_MATERIA_ID + " INTEGER, "
                + COL_APUNTE_TITULO + " TEXT NOT NULL, "
                + COL_APUNTE_RESUMEN + " TEXT, "
                + COL_APUNTE_RUTA_AUDIO + " TEXT NOT NULL, "
                + "FOREIGN KEY(" + COL_APUNTE_MATERIA_ID + ") REFERENCES materias(id) ON DELETE CASCADE"
                + ")";
        db.execSQL(CREATE_APUNTES_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Si hay una nueva versión, se eliminan las tablas y se recrean (Ideal para MVP)
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_APUNTES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MATERIAS);
        onCreate(db);
    }

    /**
     * Método público para insertar una nueva Materia en la base de datos.
     * @return El ID de la fila insertada o -1 si ocurrió un error.
     */
    public long insertarMateria(String nombre, String notionId) {
        // 1. Obtener la base de datos en modo escritura
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. Crear un contenedor de valores
        ContentValues values = new ContentValues();
        values.put(COL_MATERIA_NOMBRE, nombre);
        values.put(COL_MATERIA_NOTION_ID, notionId);

        // 3. Insertar la fila
        long id = db.insert(TABLE_MATERIAS, null, values);

        // 4. Cerrar la conexión para liberar recursos
        db.close();

        return id;
    }
    /**
     * Recupera todas las materias guardadas en SQLite.
     * @return Lista de objetos Materia.
     */
    public List<Materia> obtenerTodasLasMaterias() {
        List<Materia> listaMaterias = new ArrayList<>();

        // 1. Instanciar la base de datos en modo lectura
        SQLiteDatabase db = this.getReadableDatabase();

        // 2. Ejecutar la consulta SQL
        String query = "SELECT m.*, COUNT(a." + COL_APUNTE_ID + ") as total_apuntes " +
                "FROM " + TABLE_MATERIAS + " m " +
                "LEFT JOIN " + TABLE_APUNTES + " a ON m." + COL_MATERIA_ID + " = a." + COL_APUNTE_MATERIA_ID + " " +
                "GROUP BY m." + COL_MATERIA_ID + " " +
                "ORDER BY m." + COL_MATERIA_ID + " DESC";

        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                Materia materia = new Materia();
                materia.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_MATERIA_ID)));
                materia.setNombre(cursor.getString(cursor.getColumnIndexOrThrow(COL_MATERIA_NOMBRE)));
                materia.setNotionId(cursor.getString(cursor.getColumnIndexOrThrow(COL_MATERIA_NOTION_ID)));

                // Extraer el conteo (la columna virtual 'total_apuntes')
                materia.setCantidadApuntes(cursor.getInt(cursor.getColumnIndexOrThrow("total_apuntes")));

                listaMaterias.add(materia);
            } while (cursor.moveToNext());
        }

        // 4. Cerrar cursor y base de datos para evitar fugas de memoria
        cursor.close();
        db.close();

        return listaMaterias;
    }
    /**
     * Inserta un nuevo apunte procesado por la IA.
     */
    public long insertarApunte(long materiaId, String titulo, String resumen, String rutaAudio) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COL_APUNTE_MATERIA_ID, materiaId);
        values.put(COL_APUNTE_TITULO, titulo);
        values.put(COL_APUNTE_RESUMEN, resumen);
        values.put(COL_APUNTE_RUTA_AUDIO, rutaAudio);

        long id = db.insert(TABLE_APUNTES, null, values);
        db.close();
        return id;
    }

    public List<Apunte> obtenerApuntesPorMateria(long materiaId) {
        List<Apunte> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_APUNTES +
                " WHERE " + COL_APUNTE_MATERIA_ID + " = ? " +
                " ORDER BY " + COL_APUNTE_ID + " DESC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(materiaId)});

        if (cursor.moveToFirst()) {
            do {
                Apunte apunte = new Apunte();
                apunte.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_APUNTE_ID)));
                apunte.setMateriaId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_APUNTE_MATERIA_ID)));
                apunte.setTitulo(cursor.getString(cursor.getColumnIndexOrThrow(COL_APUNTE_TITULO)));
                apunte.setResumen(cursor.getString(cursor.getColumnIndexOrThrow(COL_APUNTE_RESUMEN)));
                apunte.setRutaAudio(cursor.getString(cursor.getColumnIndexOrThrow(COL_APUNTE_RUTA_AUDIO)));
                lista.add(apunte);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return lista;
    }
}