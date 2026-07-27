package com.uth.proyectored.client;

import com.uth.proyectored.protocol.Mensaje;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Conexion socket persistente hacia el servidor: se abre una vez con
 * conectar(), y luego se reutiliza para enviar/recibir varios mensajes.
 */
public class ClienteConexion {

    private Socket socket;
    private ObjectOutputStream salida;
    private ObjectInputStream entrada;

    public void conectar(String host, int puerto) throws IOException {
        socket = new Socket(host, puerto);
        salida = new ObjectOutputStream(socket.getOutputStream());
        salida.flush();
        entrada = new ObjectInputStream(socket.getInputStream());
    }

    public Mensaje enviarYRecibir(Mensaje mensaje) throws IOException, ClassNotFoundException {
        salida.writeObject(mensaje);
        salida.flush();
        return (Mensaje) entrada.readObject();
    }

    public void cerrar() {
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {
        }
    }
}
