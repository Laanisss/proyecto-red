package com.uth.proyectored.server;

import com.uth.proyectored.product.Producto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class ConexionBD {
private static final String URL = "jdbc:mysql://mysql.railway.internal:3306/railway?useSSL=false&serverTimezone=UTC";
private static final String USUARIO = "root";
private static final String CLAVE = "yfXRggNfdhZOoutyHtUbriOwClqNXnOk";

    private static Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, CLAVE);
    }

    public List<Producto> listarProductos() throws SQLException {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, precio, stock FROM producto ORDER BY id";

        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new Producto(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getDouble("precio"),
                        rs.getInt("stock")
                ));
            }
        }
        return lista;
    }

    public void crearProducto(Producto p) throws SQLException {
        String sql = "INSERT INTO producto (nombre, precio, stock) VALUES (?, ?, ?)";

        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, p.getNombre());
            ps.setDouble(2, p.getPrecio());
            ps.setInt(3, p.getStock());
            ps.executeUpdate();
        }
    }

    public void actualizarProducto(Producto p) throws SQLException {
        String sql = "UPDATE producto SET nombre = ?, precio = ?, stock = ? WHERE id = ?";

        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, p.getNombre());
            ps.setDouble(2, p.getPrecio());
            ps.setInt(3, p.getStock());
            ps.setInt(4, p.getId());
            int filas = ps.executeUpdate();

            if (filas == 0) {
                throw new SQLException("No existe un producto con id " + p.getId());
            }
        }
    }

    public void eliminarProducto(int id) throws SQLException {
        String sql = "DELETE FROM producto WHERE id = ?";

        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            int filas = ps.executeUpdate();

            if (filas == 0) {
                throw new SQLException("No existe un producto con id " + id);
            }
        }
    }
}
