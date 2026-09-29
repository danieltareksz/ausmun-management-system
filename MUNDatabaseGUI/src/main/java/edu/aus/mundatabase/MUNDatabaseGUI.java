package edu.aus.mundatabase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author renadsameh
 */
public class MUNDatabaseGUI {

    /*
     * FreeSQL connection details:
     *
     * Hostname: db.freesql.com
     * Port: 1521
     * Service name: 23ai_34ui2
     *
     * Because FreeSQL gives a service name, the JDBC format is:
     * jdbc:oracle:thin:@//hostname:port/service_name
     */
    public static final String DBURL
            = "jdbc:oracle:thin:@//db.freesql.com:1521/23ai_34ui2";

    public static final String DBUSER
            = "DANIELTAREKSZ_SCHEMA_7DIZZ";

    /*
     * Replace this with your newly generated FreeSQL password.
     * Do not use the password visible in the screenshot.
     */
    public static final String DBPASS
            = "CZIEVUWPEN4KIJ4a896L2VGM!EWPXB";

public static void main(String[] args) {

    System.out.println("Starting MUN Database Application...");

    try {
        Class.forName("oracle.jdbc.driver.OracleDriver");

        try (
            Connection con = DriverManager.getConnection(
                    DBURL,
                    DBUSER,
                    DBPASS
            );
            Statement statement = con.createStatement();
            ResultSet rs = statement.executeQuery(
                    "SELECT SYSDATE FROM DUAL"
            )
        ) {
            System.out.println("Connected to Oracle successfully.");

            if (rs.next()) {
                System.out.println(
                        "Current Oracle database date: "
                        + rs.getDate(1)
                );
            }
        }

        java.awt.EventQueue.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });

    } catch (ClassNotFoundException e) {
        System.out.println("Oracle JDBC driver was not found.");
        System.out.println(e.getMessage());

    } catch (SQLException e) {
        System.out.println("Could not connect to the Oracle database.");
        System.out.println("Oracle error: " + e.getMessage());
    }
}
}
