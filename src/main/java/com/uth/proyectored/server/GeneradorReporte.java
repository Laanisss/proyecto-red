package com.uth.proyectored.server;

import com.uth.proyectored.product.Producto;
import com.uth.proyectored.product.Venta;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JREmptyDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GeneradorReporte {

    public byte[] generarReporteProductos(List<Producto> productos) throws JRException {
        InputStream jrxmlStream = getClass().getClassLoader().getResourceAsStream("reporte.jrxml");
        if (jrxmlStream == null) {
            throw new JRException("No se encontro reporte.jrxml en resources");
        }

        JasperReport reporteCompilado = JasperCompileManager.compileReport(jrxmlStream);

        JRBeanCollectionDataSource fuenteDatos = new JRBeanCollectionDataSource(productos);

        Map<String, Object> parametros = new HashMap<>();
        parametros.put("titulo", "Reporte de productos");

        JasperPrint impresion = JasperFillManager.fillReport(reporteCompilado, parametros, fuenteDatos);

        try (ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            JasperExportManager.exportReportToPdfStream(impresion, salida);
            return salida.toByteArray();
        } catch (Exception e) {
            throw new JRException("Error exportando el reporte a PDF", e);
        }
    }

    public byte[] generarFactura(Venta venta) throws JRException {
        InputStream jrxmlStream = getClass().getClassLoader().getResourceAsStream("factura.jrxml");
        if (jrxmlStream == null) {
            throw new JRException("No se encontro factura.jrxml en resources");
        }

        JasperReport reporteCompilado = JasperCompileManager.compileReport(jrxmlStream);

        Map<String, Object> parametros = new HashMap<>();
        parametros.put("ventaId", venta.getId());
        parametros.put("fecha", venta.getFecha());
        parametros.put("producto", venta.getProductoNombre());
        parametros.put("cantidad", venta.getCantidad());
        parametros.put("precioUnitario", venta.getPrecioUnitario());
        parametros.put("total", venta.getTotal());

        JasperPrint impresion = JasperFillManager.fillReport(reporteCompilado, parametros, new JREmptyDataSource());

        try (ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            JasperExportManager.exportReportToPdfStream(impresion, salida);
            return salida.toByteArray();
        } catch (Exception e) {
            throw new JRException("Error exportando la factura a PDF", e);
        }
    }
}
