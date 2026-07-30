package com.uth.proyectored.product;

import java.io.Serializable;

/**
 * Lo que el servidor devuelve tras registrar una venta con exito:
 * los datos de la venta (para mostrar un resumen) y el PDF de la
 * factura ya armado, listo para guardar/abrir en el cliente.
 */
public class FacturaGenerada implements Serializable {

    private static final long serialVersionUID = 1L;

    private Venta venta;
    private byte[] pdf;

    public FacturaGenerada() {
    }

    public FacturaGenerada(Venta venta, byte[] pdf) {
        this.venta = venta;
        this.pdf = pdf;
    }

    public Venta getVenta() {
        return venta;
    }

    public void setVenta(Venta venta) {
        this.venta = venta;
    }

    public byte[] getPdf() {
        return pdf;
    }

    public void setPdf(byte[] pdf) {
        this.pdf = pdf;
    }
}
