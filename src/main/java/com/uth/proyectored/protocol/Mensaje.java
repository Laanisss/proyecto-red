package com.uth.proyectored.protocol;

import java.io.Serializable;


public class Mensaje implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Tipo {
        LISTAR_PRODUCTOS,
        CREAR_PRODUCTO,
        EDITAR_PRODUCTO,
        ELIMINAR_PRODUCTO,
        GENERAR_REPORTE,
        RESPUESTA_OK,
        RESPUESTA_ERROR,
        RESPUESTA_DATOS,
        RESPUESTA_REPORTE
    }

    private Tipo tipo;
    private Object dato;
    private String texto;

    public Mensaje() {
    }

    public Mensaje(Tipo tipo) {
        this.tipo = tipo;
    }

    public Mensaje(Tipo tipo, Object dato) {
        this.tipo = tipo;
        this.dato = dato;
    }

    public Mensaje(Tipo tipo, Object dato, String texto) {
        this.tipo = tipo;
        this.dato = dato;
        this.texto = texto;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public void setTipo(Tipo tipo) {
        this.tipo = tipo;
    }

    public Object getDato() {
        return dato;
    }

    public void setDato(Object dato) {
        this.dato = dato;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }
}
