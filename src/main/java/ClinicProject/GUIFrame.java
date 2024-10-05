package ClinicProject;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ArrayList;
import java.util.Vector;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.ResultSetMetaData;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/**
 * A GUI frame that connects that connects to the database and displays the data.
 */
public class GUIFrame {

	// Declare widgets
	private static JTable table;
	private static DefaultTableModel tableModel;
	private static JComboBox<Object> tableSelector;
	private static JTextField searchBar;
	private static JFrame frame;
	private static JPanel loginForm;

	// Declare an ArrayList to store all the table name.
	private static ArrayList<String> tables;


	/**
	 * Presents a login form, then attempts to connect to the database and display the data in a table.
	 */
	public static void start() {
		// Initialize the login form.
		loginForm = new JPanel();
		JTextField usernameField = new JTextField();
		JTextField passwordField = new JPasswordField();

		// Set layout from top to bottom
		loginForm.setLayout(new BoxLayout(loginForm, BoxLayout.Y_AXIS));

		// Adding the fields to the login form
		loginForm.add(new JLabel("Username:"));
		loginForm.add(usernameField);
		loginForm.add(new JLabel("Password:"));
		loginForm.add(passwordField);

		// Display the login form in an option pane, if the user pressed 'Cancel', then exit code 0
		int input = JOptionPane.showConfirmDialog(null, loginForm, "User Login:", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
		if (input == JOptionPane.CANCEL_OPTION) {
			System.exit(0);
		}

		// If the user clicks 'OK', attempt a connection to the database and retrieve all tables.
		try {
			DatabaseHandler.connect(usernameField.getText(), passwordField.getText());

			// Retrieve all table names to store them in the drop-down menu
			ResultSet resultTables = DatabaseHandler.displayAllTables();
			tables = new ArrayList<String>();
			while (resultTables.next()) {
				String tableName = resultTables.getString(1);
				tables.add(tableName);
			}
		} catch (SQLException e) {
			// If the connection to the database is refused or if it fails for whatever reason, 
			// display an error message with the details.
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage() + "\nError Code: " + e.getErrorCode(),
					"Error",  JOptionPane.ERROR_MESSAGE);

			// Failure to resolve a connection yields Exit Code 1001.
			System.exit(1001);

		}

		// If the connection is made without problems, proceed with the GUI interface.

		// Initialize the frame.
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle(DatabaseHandler.getURI());
		frame.setSize(600, 400);
		frame.setResizable(false);
		frame.setLayout(new BorderLayout());

		// Set a custom icon for the frame.
		ImageIcon image = new ImageIcon("R1-Main.png");
		frame.setIconImage(image.getImage());


		// Declare a top panel to hold a drop-down menu a search bar.
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());
		topPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));		// Leave a small margin


		// Initialize the drop-down menu for table selection.
		tableSelector = new JComboBox<Object>(tables.toArray());
		tableSelector.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));	// 5 Pixel margin at the right of the menu.

		// Search bar initialization.
		searchBar = new JTextField();
		topPanel.add(new JLabel("Table:"));
		topPanel.add(tableSelector, BorderLayout.WEST);
		topPanel.add(searchBar, BorderLayout.CENTER);
		frame.add(topPanel, BorderLayout.NORTH);

		// Displaying the data in a tabular form.
		tableModel = new DefaultTableModel();
		table = new JTable(tableModel);
		loadTableData();
		JScrollPane scrollPane = new JScrollPane(table);
		frame.add(scrollPane, BorderLayout.CENTER);

		// Declaring buttons
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

		JButton insertButton = new JButton("Add");
		JButton deleteButton = new JButton("Remove");
		JButton closeButton = new JButton("Exit");

		buttonPanel.add(insertButton);
		buttonPanel.add(deleteButton);
		buttonPanel.add(closeButton);

		frame.add(buttonPanel, BorderLayout.SOUTH);

		// Finally, display the frame.
		frame.setVisible(true);

		// Events Handlers:
		searchBar.getDocument().addDocumentListener(new DocumentListener() {
			public void insertUpdate(DocumentEvent e) {
				searchTable();		// Executes only when writing into the search bar.

			}

			public void removeUpdate(DocumentEvent e) {
				searchTable();		// Executes only when deleting from the search bar.

			}

			public void changedUpdate(DocumentEvent e) {}	// For styling changes, interface requires it to be declared.
		});

		tableSelector.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadTableData();							// Load elements when a new table is selected.
			}
		});



		insertButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Prompt the user for data and then update the table with the newly created element.
				showInsertDialog();
				loadTableData();
			}
		});

		deleteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Delete the row and update the table.
				deleteSelectedRow();
				loadTableData();
			}
		});

		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Exit the program.
				System.exit(0);
			}
		});
	}


	/**
	 * Executes when input is provided to the search bar, Filters the table to rows that match the search term.
	 */
	private static void searchTable() {
		DefaultTableModel ob=(DefaultTableModel) table.getModel();
		TableRowSorter<DefaultTableModel> obj = new TableRowSorter<DefaultTableModel>(ob);
		table.setRowSorter(obj);
		obj.setRowFilter(RowFilter.regexFilter(searchBar.getText()));		// Filter the table to only display what is on the search bar.
	}


	/**
	 * Sends a GET request to the database, and displays the data in a tabular form.
	 */
	private static void loadTableData() {
		// Get the selected table from the drop-down menu.
		String selectedTable = (String) tableSelector.getSelectedItem();

		// Clear the table.
		tableModel.setRowCount(0);
		tableModel.setColumnCount(0);

		// Attempt to retrieve all table entries from the database
		try {
			String query = "SELECT * FROM " + selectedTable;
			ResultSetMetaData metaData;
			int columnCount;
			System.out.println(query);
			ResultSet rs = DatabaseHandler.executeSelectQuery(query);
			metaData = rs.getMetaData();
			columnCount = metaData.getColumnCount();
			// Display columns:
			for (int i = 1; i <= columnCount; i++) {
				tableModel.addColumn(metaData.getColumnName(i));
			}

			// Display row entries:
			while (rs.next()) {
				Vector<Object> row = new Vector<Object>();
				for (int i = 1; i <= columnCount; i++) {
					row.add(rs.getObject(i));
				}
				tableModel.addRow(row);
			}
			searchBar.setText("");		// Clear the search bar.

		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage() + "\nError Code: " + e.getErrorCode(),
					"Error",  JOptionPane.ERROR_MESSAGE);

			// Failure to retrieve data yields: Exit Code 1004
			System.exit(1004);
		}
	}


	/**
	 * Provides a form containing field for all the table's columns, Then constructs a query and sends it to the server.
	 * And finally, update the GUI table.
	 */
	private static void showInsertDialog() {
		String selectedTable = (String) tableSelector.getSelectedItem();
		ArrayList<String> columnList = new ArrayList<String>();

		try {
			// Get all table columns and add them to an arraylist.
			ResultSet columnsRS = DatabaseHandler.getConnection().getMetaData().getColumns(null, null, selectedTable, null);
			while (columnsRS.next()) {
				String columnName = columnsRS.getString("COLUMN_NAME");
				if (columnsRS.getString("IS_AUTOINCREMENT").equals("YES")) continue;	// Skip ID Column, or any auto assigned table column.
				columnList.add(columnName);
			}

			// Create an input field for each column from above.
			String[] columns = columnList.toArray(new String[0]);
			JTextField[] fields = new JTextField[columns.length];

			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			for (int i = 0; i < columns.length; i++) {
				fields[i] = new JTextField(20);
				panel.add(new JLabel(columns[i] + ":"));
				panel.add(fields[i]);
			}

			int result = JOptionPane.showConfirmDialog(null, panel, "Add entry:", JOptionPane.OK_CANCEL_OPTION);

			if (result == JOptionPane.OK_OPTION) {
				// Construct the query and execute it.
				String query = "INSERT INTO " + selectedTable + "(";

				for (int i = 0; i < columns.length; i++) {
					query += columns[i];
					if ((i + 1) != columns.length) query += ", "; 
				}
				query += ") VALUES \n(\n\t\"";
				for (int i = 0; i < fields.length; i++) {
					query += fields[i].getText().trim();
					if ((i + 1) != fields.length) query += "\",\n\t\""; 
				}
				query += "\"\n);";
				System.out.println(query);
				DatabaseHandler.executeUpdateQuery(query);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage() + "\nError Code: " + e.getErrorCode(),
					"Error",  JOptionPane.ERROR_MESSAGE);

			// Failure to insert a new entry yields: Exit Code 1005.
			System.exit(1005);
		}
	}


	/**
	 * Deletes the selected row from the database, then updates the GUI table.
	 */
	private static void deleteSelectedRow() {
		// Retrieve the current table.
		String selectedTable = (String) tableSelector.getSelectedItem();

		// Retrieve the current entry to remove.
		int selectedRow = table.getSelectedRow();

		// If no row is highlighted, display a warning message.
		if (selectedRow == -1) {
			JOptionPane.showMessageDialog(frame, "Please select an item to remove.", "No Row Selected!", JOptionPane.WARNING_MESSAGE);
			return;
		}

		// Retrieve the ID of the entry.
		Object idRow = table.getValueAt(selectedRow, 0);
		String query = "DELETE FROM " + selectedTable + " WHERE ID=" + "'" + idRow + "'";

		// Attempt to delete the row with the given ID.
		try {
			System.out.println(query);
			DatabaseHandler.executeUpdateQuery(query);			
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage() + "\nError Code: " + e.getErrorCode(),
					"Error",  JOptionPane.ERROR_MESSAGE);

			// Failure to delete an entry yields Exit Code 1006
			System.exit(1006);
		}

	}

}
