package com.uth.proyectored.client;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;

public class ClienteApp {

    public static void main(String[] args) {
        FlatLightLaf.setup();

        SwingUtilities.invokeLater(() -> {
            VentanaPrincipal ventana = new VentanaPrincipal();
            ventana.setVisible(true);
        });
    }
}
