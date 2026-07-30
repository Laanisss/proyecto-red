package com.uth.proyectored.product;

import java.io.Serializable;

/**
 * Registro de una venta ya realizada (una fila de la tabla `venta`).
 * Guarda el nombre y precio del producto EN EL MOMENTO de la venta,
 * para que el historial no cambie si despues editas o borras el producto.
 */
public class Venta implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private int productoId;
    private String productoNombre;
    private int cantidad;
    private double precioUnitario;
    private double total;
    private String fecha;

    public Venta() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProductoId() {
        return productoId;
    }

    public void setProductoId(int productoId) {
        this.productoId = productoId;
    }

    public String getProductoNombre() {
        return productoNombre;
    }

    public void setProductoNombre(String productoNombre) {
        this.productoNombre = productoNombre;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }
}
