package trabalho.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Db {
    // URL EXTERNA do Render com SSL habilitado
    private static final String URL = "jdbc:postgresql://dpg-d4gh63mmcj7s73bdmokg-a.oregon-postgres.render.com:5432/muse_3jg2";
    private static final String USER = "root";
    private static final String PASS = "SG1KEurzAoFTEIoyOc5hFTwzA98wIYMZ";

    public static Connection getConnection() throws SQLException {
        try {
            // Carrega o driver PostgreSQL
            Class.forName("org.postgresql.Driver");

            // Retorna a conexão
            return DriverManager.getConnection(URL, USER, PASS);

        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver PostgreSQL não encontrado", e);
        }
    }
}