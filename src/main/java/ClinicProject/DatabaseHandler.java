package ClinicProject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * 
 * The DatabaseHandler classes manages connections to the database and performs CRUD operations.
 *
 */
public class DatabaseHandler {
	private static Connection connection;
	private static final String URI = "jdbc:mysql://127.0.0.1:3306/records";	// Connect to a local SQL-Server.
	private static Statement statement;

	/**
	 * 
	 * @return Returns the URI for the connection.
	 */
	public static String getURI() {
		return URI;
	}

	/**
	 * 
	 * @param username The user-name for the database connection
	 * @param password	The password for the database connection
	 * @throws SQLException Will throw an error if the connection is not established.
	 */
	public static void connect(String username, String password) throws SQLException {
		connection = DriverManager.getConnection(URI, username, password);	// Requires a dependency, Check the pom file from line 7 to 13.
	}

	/**
	 * Method executing only READ statements. Returns a ResultSet consisting of Rows and Columns.
	 * 
	 * @param query The select query that is to be sent to the server to be executed.
	 * @return	Returns A result set containing the selected rows.
	 * @throws SQLException Will throw an exception if the server doesn't respond properly.
	 */
	public static ResultSet executeSelectQuery(String query) throws SQLException {
		statement = connection.createStatement();
		ResultSet resultSet = statement.executeQuery(query);
		return resultSet;
	}

	/**
	 * Method executing only UPDATE statements. Can create, update, or delete rows from the database. 
	 * @param query The UPDATE statement that is to be sent to the server to be executed.
	 * @throws SQLException Will throw an exception if the server doesn't respond properly.
	 */
	public static void executeUpdateQuery(String query) throws SQLException {
		// Make sure that connection is initialized.
		if (connection == null) {
			System.out.println("Connection is closed!");
			return;
		}

		statement = connection.createStatement();
		statement.executeUpdate(query);

	}

	/**
	 * Lists all the tables in the database.
	 * @return Returns a resultSet containing the table names.
	 * @throws SQLException Will throw an exception if the server doesn't respond properly.
	 */
	public static ResultSet displayAllTables() throws SQLException {
		statement = connection.createStatement();
		ResultSet resultSet = statement.executeQuery("SHOW TABLES");
		return resultSet;
	}


	/**
	 * Close the connection to prevent memory leaks.
	 */
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

	/**
	 * @return Returns the connection.
	 */
	public static Connection getConnection() {
		return connection;
	}

}

