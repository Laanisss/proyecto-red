package com.uth.proyectored.server;

import com.uth.proyectored.product.Producto;
import com.uth.proyectored.protocol.Mensaje;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.List;

public class ManejadorCliente extends Thread {

    private final Socket socket;
    private final ConexionBD conexionBD = new ConexionBD();
    private final GeneradorReporte generadorReporte = new GeneradorReporte();

    public ManejadorCliente(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        String direccionCliente = socket.getInetAddress().getHostAddress();
        System.out.println("Cliente conectado desde: " + direccionCliente);

        try (ObjectOutputStream salida = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream())) {

            Mensaje peticion;
            while ((peticion = (Mensaje) entrada.readObject()) != null) {
                Mensaje respuesta = procesar(peticion);
                salida.writeObject(respuesta);
                salida.flush();
            }

        } catch (EOFException | SocketException fin) {
            System.out.println("Cliente desconectado: " + direccionCliente);
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error con el cliente " + direccionCliente + ": " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }

    private Mensaje procesar(Mensaje peticion) {
        try {
            switch (peticion.getTipo()) {

                case LISTAR_PRODUCTOS -> {
                    List<Producto> productos = conexionBD.listarProductos();
                    return new Mensaje(Mensaje.Tipo.RESPUESTA_DATOS, productos);
                }

                case CREAR_PRODUCTO -> {
                    Producto nuevo = (Producto) peticion.getDato();
                    conexionBD.crearProducto(nuevo);
                    return new Mensaje(Mensaje.Tipo.RESPUESTA_OK, null, "Producto agregado");
                }

                case GENERAR_REPORTE -> {
                    List<Producto> productos = conexionBD.listarProductos();
                    byte[] pdf = generadorReporte.generarReporteProductos(productos);
                    return new Mensaje(Mensaje.Tipo.RESPUESTA_REPORTE, pdf);
                }

                default -> {
                    return new Mensaje(Mensaje.Tipo.RESPUESTA_ERROR, null, "Tipo de mensaje no reconocido");
                }
            }
        } catch (SQLException e) {
            return new Mensaje(Mensaje.Tipo.RESPUESTA_ERROR, null, "Error de base de datos: " + e.getMessage());
        } catch (Exception e) {
            return new Mensaje(Mensaje.Tipo.RESPUESTA_ERROR, null, "Error generando el reporte: " + e.getMessage());
        }
    }
}
