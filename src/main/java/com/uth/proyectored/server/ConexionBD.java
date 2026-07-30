package com.uth.proyectored.server;

import com.uth.proyectored.product.Producto;
import com.uth.proyectored.product.Venta;

import java.sql.*;
import java.time.format.DateTimeFormatter;
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

    /**
     * Registra una venta de forma atomica: verifica stock, lo descuenta,
     * e inserta el registro en `venta` -- todo en una sola transaccion,
     * para que dos ventas al mismo tiempo nunca dejen el stock mal.
     * El precio se toma de la base de datos en este momento, nunca del
     * cliente, para que nadie pueda mandar un precio/total falso.
     */
    public Venta registrarVenta(int productoId, int cantidad) throws SQLException {
        if (cantidad <= 0) {
            throw new SQLException("La cantidad debe ser mayor a cero");
        }

        Connection con = null;
        try {
            con = obtenerConexion();
            con.setAutoCommit(false);

            String nombre;
            double precio;
            int stockActual;

            String sqlSeleccionar = "SELECT nombre, precio, stock FROM producto WHERE id = ? FOR UPDATE";
            try (PreparedStatement ps = con.prepareStatement(sqlSeleccionar)) {
                ps.setInt(1, productoId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("El producto ya no existe");
                    }
                    nombre = rs.getString("nombre");
                    precio = rs.getDouble("precio");
                    stockActual = rs.getInt("stock");
                }
            }

            if (stockActual < cantidad) {
                throw new SQLException("Stock insuficiente. Disponible: " + stockActual);
            }

            String sqlActualizar = "UPDATE producto SET stock = stock - ? WHERE id = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlActualizar)) {
                ps.setInt(1, cantidad);
                ps.setInt(2, productoId);
                ps.executeUpdate();
            }

            double total = precio * cantidad;
            int ventaId;
            String sqlInsertar = "INSERT INTO venta (producto_id, producto_nombre, cantidad, precio_unitario, total) "
                    + "VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sqlInsertar, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, productoId);
                ps.setString(2, nombre);
                ps.setInt(3, cantidad);
                ps.setDouble(4, precio);
                ps.setDouble(5, total);
                ps.executeUpdate();
                try (ResultSet claves = ps.getGeneratedKeys()) {
                    claves.next();
                    ventaId = claves.getInt(1);
                }
            }

            con.commit();

            Venta venta = new Venta();
            venta.setId(ventaId);
            venta.setProductoId(productoId);
            venta.setProductoNombre(nombre);
            venta.setCantidad(cantidad);
            venta.setPrecioUnitario(precio);
            venta.setTotal(total);
            venta.setFecha(java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            return venta;

        } catch (SQLException e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ignored) {
                }
            }
            throw e;
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    public List<Venta> listarVentas() throws SQLException {
        List<Venta> lista = new ArrayList<>();
        String sql = "SELECT id, producto_id, producto_nombre, cantidad, precio_unitario, total, fecha "
                + "FROM venta ORDER BY fecha DESC, id DESC";

        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Venta v = new Venta();
                v.setId(rs.getInt("id"));
                v.setProductoId(rs.getInt("producto_id"));
                v.setProductoNombre(rs.getString("producto_nombre"));
                v.setCantidad(rs.getInt("cantidad"));
                v.setPrecioUnitario(rs.getDouble("precio_unitario"));
                v.setTotal(rs.getDouble("total"));

                Timestamp ts = rs.getTimestamp("fecha");
                v.setFecha(ts != null ? ts.toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "");

                lista.add(v);
            }
        }
        return lista;
    }
}
