package ClinicProject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

public class DatabaseHandler {
	private static Connection connection;
	private static final String URI = "jdbc:mysql://127.0.0.1:3306/records";	// Connect to a local SQL-Server.
	private static Statement statement;

	public static String getURI() {
		return URI;
	}
	
	// Connect with the user-name and password, SQLExceptions must be handled outside of the class.
	public static void connect(String username, String password) throws SQLException {
		connection = DriverManager.getConnection(URI, username, password);	// Requires a dependency, Check the pom file from line 7 to 13.
	}

	// Method executing only SELECT statements. Returns a ResultSet consisting of Rows and Columns.
	public static ResultSet executeSelectQuery(String query) throws SQLException {
		statement = connection.createStatement();
		ResultSet resultSet = statement.executeQuery(query);
		return resultSet;
	}

	// Performs all UPDATE functions, does not produce ResultSets
	public static void executeUpdateQuery(String query) throws SQLException {
		// Make sure that connection is initialized.
		if (connection == null) {
			System.out.println("Connection is closed!");
			return;
		}

		statement = connection.createStatement();
		statement.executeUpdate(query);

	}

	// List all tables.
	public static ResultSet displayAllTables() throws SQLException {
		statement = connection.createStatement();
		ResultSet resultSet = statement.executeQuery("SHOW TABLES");
		return resultSet;
	}

	// Finally, make sure that the connection is closed, to avoid memory leaks.
	public static void closeConnection() {
		if (connection != null) {
			try {
				connection.close();
			} catch(SQLException e) {
				e.printStackTrace();
			} finally {
				connection = null;
			}
		}
	}

	public static Connection getConnection() {
		return connection;
	}

}

