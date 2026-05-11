package com.CrisDLS.apuntesia.models;

public class Apunte {
    private long id;
    private long materiaId;
    private String titulo;
    private String resumen;
    private String rutaAudio;

    public Apunte() {}

    public Apunte(long materiaId, String titulo, String resumen, String rutaAudio) {
        this.materiaId = materiaId;
        this.titulo = titulo;
        this.resumen = resumen;
        this.rutaAudio = rutaAudio;
    }

    // Getters y Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getMateriaId() { return materiaId; }
    public void setMateriaId(long materiaId) { this.materiaId = materiaId; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getResumen() { return resumen; }
    public void setResumen(String resumen) { this.resumen = resumen; }

    public String getRutaAudio() { return rutaAudio; }
    public void setRutaAudio(String rutaAudio) { this.rutaAudio = rutaAudio; }
}
