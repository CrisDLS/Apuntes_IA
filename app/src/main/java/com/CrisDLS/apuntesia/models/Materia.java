package com.CrisDLS.apuntesia.models;

public class Materia {
    private long id;
    private String nombre;
    private String notionId;
    private int cantidadApuntes;

    // Constructor vacío requerido por buenas prácticas
    public Materia() {}

    // Constructor sin ID (Ideal para crear objetos antes de insertarlos a la BD)
    public Materia(String nombre, String notionId) {
        this.nombre = nombre;
        this.notionId = notionId;
    }

    // Constructor completo
    public Materia(long id, String nombre, String notionId) {
        this.id = id;
        this.nombre = nombre;
        this.notionId = notionId;
    }

    // Getters y Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getNotionId() { return notionId; }
    public void setNotionId(String notionId) { this.notionId = notionId; }
    public int getCantidadApuntes() { return cantidadApuntes; }
    public void setCantidadApuntes(int cantidadApuntes) { this.cantidadApuntes = cantidadApuntes; }
}

