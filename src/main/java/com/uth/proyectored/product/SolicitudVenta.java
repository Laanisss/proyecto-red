package com.uth.proyectored.product;

import java.io.Serializable;


public class SolicitudVenta implements Serializable {

    private static final long serialVersionUID = 1L;

    private int productoId;
    private int cantidad;

    public SolicitudVenta() {
    }

    public SolicitudVenta(int productoId, int cantidad) {
        this.productoId = productoId;
        this.cantidad = cantidad;
    }

    public int getProductoId() {
        return productoId;
    }

    public void setProductoId(int productoId) {
        this.productoId = productoId;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
}
