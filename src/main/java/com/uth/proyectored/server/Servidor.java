package com.uth.proyectored.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class Servidor {

    public static final int PUERTO = 5000;

    public static void main(String[] args) {
        System.out.println("Iniciando servidor en el puerto " + PUERTO + " ...");

        try (ServerSocket servidorSocket = new ServerSocket(PUERTO)) {
            System.out.println("Servidor listo. Esperando conexiones...");

            while (true) {
                Socket socketCliente = servidorSocket.accept();
                new ManejadorCliente(socketCliente).start();
            }

        } catch (IOException e) {
            System.err.println("No se pudo iniciar el servidor: " + e.getMessage());
        }
    }
}
