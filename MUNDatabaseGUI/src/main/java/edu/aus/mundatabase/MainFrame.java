/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package edu.aus.mundatabase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author renadsameh
 */
public class MainFrame extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger
            = java.util.logging.Logger.getLogger(MainFrame.class.getName());

    /**
     * Creates new form MainFrame
     */
    public MainFrame() {
    initComponents();

 
    setTitle("MUN Conference Database Management System");
    setSize(1000, 700);
    setLocationRelativeTo(null);

    configureTables();
    configureTableHeaders();
    clearDefaultValues();
    loadCountries();
    loadCommittees();
    loadDelegates();

    clearCommitteeFields();
    loadCommitteeTable();

    loadAttendanceDelegates();
    loadAttendanceSessions();
    loadAttendanceStatuses();
    loadAttendanceTable();
    loadPaperDelegates();
    loadPaperCommittees();
    loadPositionPapersTable();
    clearPaperFields();
}
    private boolean isValidEmail(String email) {
    return email.matches(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
}
    private boolean isValidDate(String dateText) {

    try {
        java.time.format.DateTimeFormatter formatter
                = new java.time.format.DateTimeFormatterBuilder()
                        .parseCaseInsensitive()
                        .appendPattern("dd-MMM-uuuu")
                        .toFormatter(java.util.Locale.ENGLISH)
                        .withResolverStyle(
                                java.time.format.ResolverStyle.STRICT
                        );

        java.time.LocalDate.parse(dateText, formatter);

        return true;

    } catch (java.time.format.DateTimeParseException e) {
        return false;
    }
}
    private void configureTableHeaders() {

    delegatesTable.getTableHeader().setReorderingAllowed(false);
    committeesTable.getTableHeader().setReorderingAllowed(false);
    attendanceTable.getTableHeader().setReorderingAllowed(false);
    positionPapersTable.getTableHeader().setReorderingAllowed(false);
    reportsTable.getTableHeader().setReorderingAllowed(false);
}
    private void configureTables() {

    delegatesTable.setSelectionMode(
            javax.swing.ListSelectionModel.SINGLE_SELECTION
    );

    committeesTable.setSelectionMode(
            javax.swing.ListSelectionModel.SINGLE_SELECTION
    );

    attendanceTable.setSelectionMode(
            javax.swing.ListSelectionModel.SINGLE_SELECTION
    );

    positionPapersTable.setSelectionMode(
            javax.swing.ListSelectionModel.SINGLE_SELECTION
    );

    reportsTable.setSelectionMode(
            javax.swing.ListSelectionModel.SINGLE_SELECTION
    );
}
    private void clearPaperFields() {
    paperNumberField.setEditable(true);
    paperNumberField.setText("");
    submissionDateField.setText("");
    contentTextArea.setText("");
    scoreField.setText("");

    if (paperDelegateComboBox.getItemCount() > 0) {
        paperDelegateComboBox.setSelectedIndex(0);
    }

    if (paperCommitteeComboBox.getItemCount() > 0) {
        paperCommitteeComboBox.setSelectedIndex(0);
    }

    paperNumberField.requestFocus();
    
}
    private void loadPaperDelegates() {

    String sql = """
            SELECT d_id,
                   f_name || ' ' || l_name AS delegate_name
            FROM delegate
            ORDER BY d_id
            """;

    paperDelegateComboBox.removeAllItems();

    try (
        Connection con = DriverManager.getConnection(
                MUNDatabaseGUI.DBURL,
                MUNDatabaseGUI.DBUSER,
                MUNDatabaseGUI.DBPASS
        );
        PreparedStatement statement = con.prepareStatement(sql);
        ResultSet resultSet = statement.executeQuery()
    ) {

        while (resultSet.next()) {

            String item = resultSet.getInt("d_id")
                    + " - "
                    + resultSet.getString("delegate_name");

            paperDelegateComboBox.addItem(item);
        }

    } catch (SQLException e) {

        JOptionPane.showMessageDialog(
                this,
                "Could not load delegates for position papers.\n\n"
                + "Oracle error: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
    private void loadPaperCommittees() {

    String sql = """
            SELECT com_id,
                   com_name
            FROM committee
            ORDER BY com_id
            """;

    paperCommitteeComboBox.removeAllItems();

    try (
        Connection con = DriverManager.getConnection(
                MUNDatabaseGUI.DBURL,
                MUNDatabaseGUI.DBUSER,
                MUNDatabaseGUI.DBPASS
        );
        PreparedStatement statement = con.prepareStatement(sql);
        ResultSet resultSet = statement.executeQuery()
    ) {

        while (resultSet.next()) {

            String item = resultSet.getInt("com_id")
                    + " - "
                    + resultSet.getString("com_name");

            paperCommitteeComboBox.addItem(item);
        }

    } catch (SQLException e) {

        JOptionPane.showMessageDialog(
                this,
                "Could not load committees for position papers.\n\n"
                + "Oracle error: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
    private void loadPositionPapersTable() {

    String sql = """
            SELECT pp.paper_no,
                   TO_CHAR(pp.sub_date, 'DD-MON-YYYY') AS sub_date,
                   pp.content,
                   pp.score,
                   pp.d_id,
                   d.f_name || ' ' || d.l_name AS delegate_name,
                   pp.com_id,
                   cm.com_name
            FROM position_paper pp
            JOIN delegate d
                ON pp.d_id = d.d_id
            JOIN committee cm
                ON pp.com_id = cm.com_id
            ORDER BY pp.paper_no
            """;

    String[] columnNames = {
        "Paper Number",
        "Submission Date",
        "Content",
        "Score",
        "Delegate ID",
        "Delegate Name",
        "Committee ID",
        "Committee"
    };

    DefaultTableModel model = new DefaultTableModel(columnNames, 0) {

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    try (
        Connection con = DriverManager.getConnection(
                MUNDatabaseGUI.DBURL,
                MUNDatabaseGUI.DBUSER,
                MUNDatabaseGUI.DBPASS
        );
        PreparedStatement statement = con.prepareStatement(sql);
        ResultSet resultSet = statement.executeQuery()
    ) {

        while (resultSet.next()) {

            model.addRow(new Object[]{
                resultSet.getInt("paper_no"),
                resultSet.getString("sub_date"),
                resultSet.getString("content"),
                resultSet.getBigDecimal("score"),
                resultSet.getInt("d_id"),
                resultSet.getString("delegate_name"),
                resultSet.getInt("com_id"),
                resultSet.getString("com_name")
            });
        }

        positionPapersTable.setModel(model);

    } catch (SQLException e) {

        JOptionPane.showMessageDialog(
                this,
                "Could not load position papers.\n\n"
                + "Oracle error: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
   

    private void clearDefaultValues() {
        delegateIdField.setText("");
        firstNameField.setText("");
        lastNameField.setText("");
        emailField.setText("");
        schoolField.setText("");
        dobField.setText("");

        statusComboBox.removeAllItems();
        statusComboBox.addItem("Registered");
        statusComboBox.addItem("Pending");
        statusComboBox.addItem("Waitlisted");
        statusComboBox.addItem("Cancelled");

        countryComboBox.removeAllItems();
        committeeComboBox.removeAllItems();
    }

    private void loadCountries() {

        String sql = """
                SELECT country_name
                FROM country
                ORDER BY country_name
                """;

        try (
            Connection con = DriverManager.getConnection(
                    MUNDatabaseGUI.DBURL,
                    MUNDatabaseGUI.DBUSER,
                    MUNDatabaseGUI.DBPASS
            );
            PreparedStatement statement = con.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery()
        ) {

            countryComboBox.removeAllItems();

            while (resultSet.next()) {
                countryComboBox.addItem(
                        resultSet.getString("country_name")
                );
            }

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Could not load countries.\n\n"
                    + "Oracle error: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void loadCommittees() {

        String sql = """
                SELECT com_name
                FROM committee
                ORDER BY com_name
                """;

        try (
            Connection con = DriverManager.getConnection(
                    MUNDatabaseGUI.DBURL,
                    MUNDatabaseGUI.DBUSER,
                    MUNDatabaseGUI.DBPASS
            );
            PreparedStatement statement = con.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery()
        ) {

            committeeComboBox.removeAllItems();

            while (resultSet.next()) {
                committeeComboBox.addItem(
                        resultSet.getString("com_name")
                );
            }

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Could not load committees.\n\n"
                    + "Oracle error: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void loadDelegates() {

        String sql = """
                SELECT d.d_id,
                       d.f_name,
                       d.l_name,
                       d.email,
                       d.school,
                       TO_CHAR(d.dob, 'DD-MON-YYYY') AS dob,
                       d.regi_status,
                       c.country_name,
                       cm.com_name
                FROM delegate d
                JOIN country c
                    ON d.country_id = c.country_id
                JOIN committee cm
                    ON d.com_id = cm.com_id
                ORDER BY d.d_id
                """;

        String[] columnNames = {
            "Delegate ID",
            "First Name",
            "Last Name",
            "Email",
            "School",
            "Date of Birth",
            "Status",
            "Country",
            "Committee"
        };

        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        try (
            Connection con = DriverManager.getConnection(
                    MUNDatabaseGUI.DBURL,
                    MUNDatabaseGUI.DBUSER,
                    MUNDatabaseGUI.DBPASS
            );
            PreparedStatement statement = con.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery()
        ) {

            while (resultSet.next()) {

                Object[] row = {
                    resultSet.getInt("d_id"),
                    resultSet.getString("f_name"),
                    resultSet.getString("l_name"),
                    resultSet.getString("email"),
                    resultSet.getString("school"),
                    resultSet.getString("dob"),
                    resultSet.getString("regi_status"),
                    resultSet.getString("country_name"),
                    resultSet.getString("com_name")
                };

                model.addRow(row);
            }

            delegatesTable.setModel(model);

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Could not load delegates.\n\n"
                    + "Oracle error: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
    private void clearDelegateFields() {
    committeeIdField.setEditable(true);
    delegateIdField.setText("");
    firstNameField.setText("");
    lastNameField.setText("");
    emailField.setText("");
    schoolField.setText("");
    dobField.setText("");

    statusComboBox.setSelectedIndex(0);

    if (countryComboBox.getItemCount() > 0) {
        countryComboBox.setSelectedIndex(0);
    }

    if (committeeComboBox.getItemCount() > 0) {
        committeeComboBox.setSelectedIndex(0);
    }

    delegateIdField.requestFocus();
    delegateIdField.setEditable(false);
}
    private void clearCommitteeFields() {
    committeeIdField.setText("");
    committeeNameField.setText("");
    committeeTypeField.setText("");
    buildingNameField.setText("");
    roomNumberField.setText("");

    committeeIdField.requestFocus();
    
}
    private void loadCommitteeTable() {

    String sql = """
            SELECT com_id,
                   com_name,
                   com_type,
                   building_name,
                   room_no
            FROM committee
            ORDER BY com_id
            """;

    String[] columnNames = {
        "Committee ID",
        "Committee Name",
        "Committee Type",
        "Building Name",
        "Room Number"
    };

    DefaultTableModel model = new DefaultTableModel(columnNames, 0) {

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    try (
        Connection con = DriverManager.getConnection(
                MUNDatabaseGUI.DBURL,
                MUNDatabaseGUI.DBUSER,
                MUNDatabaseGUI.DBPASS
        );
        PreparedStatement statement = con.prepareStatement(sql);
        ResultSet resultSet = statement.executeQuery()
    ) {

        while (resultSet.next()) {

            Object[] row = {
                resultSet.getInt("com_id"),
                resultSet.getString("com_name"),
                resultSet.getString("com_type"),
                resultSet.getString("building_name"),
                resultSet.getString("room_no")
            };

            model.addRow(row);
        }

        committeesTable.setModel(model);

    } catch (SQLException e) {

        JOptionPane.showMessageDialog(
                this,
                """
                Could not load committees.
                
                Oracle error: """ + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
    private void loadAttendanceDelegates() {

    String sql = """
            SELECT d_id,
                   f_name || ' ' || l_name AS delegate_name
            FROM delegate
            ORDER BY d_id
            """;

    delegateAttendanceComboBox.removeAllItems();

    try (
        Connection con = DriverManager.getConnection(
                MUNDatabaseGUI.DBURL,
                MUNDatabaseGUI.DBUSER,
                MUNDatabaseGUI.DBPASS
        );
        PreparedStatement statement = con.prepareStatement(sql);
        ResultSet resultSet = statement.executeQuery()
    ) {

        while (resultSet.next()) {

            String item = resultSet.getInt("d_id")
                    + " - "
                    + resultSet.getString("delegate_name");

            delegateAttendanceComboBox.addItem(item);
        }

    } catch (SQLException e) {

        JOptionPane.showMessageDialog(
                this,
                "Could not load delegates for attendance.\n\n"
                + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
    private void loadAttendanceSessions() {

    String sql = """
            SELECT session_id,
                   TO_CHAR(session_date, 'DD-MON-YYYY') AS session_date,
                   s_starttime,
                   s_endtime
            FROM mun_session
            ORDER BY session_date, s_starttime
            """;

    sessionAttendanceComboBox.removeAllItems();

    try (
        Connection con = DriverManager.getConnection(
                MUNDatabaseGUI.DBURL,
                MUNDatabaseGUI.DBUSER,
                MUNDatabaseGUI.DBPASS
        );
        PreparedStatement statement = con.prepareStatement(sql);
        ResultSet resultSet = statement.executeQuery()
    ) {

        while (resultSet.next()) {

            String item = resultSet.getInt("session_id")
                    + " - "
                    + resultSet.getString("session_date")
                    + " "
                    + resultSet.getString("s_starttime")
                    + "-"
                    + resultSet.getString("s_endtime");

            sessionAttendanceComboBox.addItem(item);
        }

    } catch (SQLException e) {

        JOptionPane.showMessageDialog(
                this,
                "Could not load sessions.\n\n"
                + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
    private void loadAttendanceStatuses() {

    statusAttendanceComboBox.removeAllItems();

    statusAttendanceComboBox.addItem("present");
    statusAttendanceComboBox.addItem("present_and_voting");
    statusAttendanceComboBox.addItem("absent");
    statusAttendanceComboBox.addItem("late");
}
    private void loadAttendanceTable() {

    String sql = """
            SELECT a.d_id,
                   d.f_name || ' ' || d.l_name AS delegate_name,
                   a.session_id,
                   TO_CHAR(s.session_date, 'DD-MON-YYYY') AS session_date,
                   s.s_starttime,
                   s.s_endtime,
                   a.status
            FROM attendance a
            JOIN delegate d
                ON a.d_id = d.d_id
            JOIN mun_session s
                ON a.session_id = s.session_id
            ORDER BY a.d_id, a.session_id
            """;

    String[] columnNames = {
        "Delegate ID",
        "Delegate Name",
        "Session ID",
        "Session Date",
        "Start Time",
        "End Time",
        "Status"
    };

    DefaultTableModel model = new DefaultTableModel(columnNames, 0) {

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    try (
        Connection con = DriverManager.getConnection(
                MUNDatabaseGUI.DBURL,
                MUNDatabaseGUI.DBUSER,
                MUNDatabaseGUI.DBPASS
        );
        PreparedStatement statement = con.prepareStatement(sql);
        ResultSet resultSet = statement.executeQuery()
    ) {

        while (resultSet.next()) {

            model.addRow(new Object[]{
                resultSet.getInt("d_id"),
                resultSet.getString("delegate_name"),
                resultSet.getInt("session_id"),
                resultSet.getString("session_date"),
                resultSet.getString("s_starttime"),
                resultSet.getString("s_endtime"),
                resultSet.getString("status")
            });
        }

        attendanceTable.setModel(model);

    } catch (SQLException e) {

        JOptionPane.showMessageDialog(
                this,
                "Could not load attendance records.\n\n"
                + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
    private void displayReport(
        String sql,
        String[] columnNames,
        String[] databaseColumns) {

    DefaultTableModel model = new DefaultTableModel(columnNames, 0) {

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    try (
        Connection con = DriverManager.getConnection(
                MUNDatabaseGUI.DBURL,
                MUNDatabaseGUI.DBUSER,
                MUNDatabaseGUI.DBPASS
        );
        PreparedStatement statement = con.prepareStatement(sql);
        ResultSet resultSet = statement.executeQuery()
    ) {

        while (resultSet.next()) {

            Object[] row = new Object[databaseColumns.length];

            for (int i = 0; i < databaseColumns.length; i++) {
                row[i] = resultSet.getObject(databaseColumns[i]);
            }

            model.addRow(row);
        }

        reportsTable.setModel(model);
        reportsTable.setAutoResizeMode(
        javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS
);

        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "The query returned no records.",
                    "Report",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }

    } catch (SQLException e) {

        JOptionPane.showMessageDialog(
                this,
                "Could not display report.\n\n"
                + "Oracle error: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        committeesPanel = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        committeeIdField = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        committeeNameField = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        committeeTypeField = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        buildingNameField = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        roomNumberField = new javax.swing.JTextField();
        addCommitteeButton = new javax.swing.JButton();
        updateCommitteeButton = new javax.swing.JButton();
        deleteCommitteeButton = new javax.swing.JButton();
        searchCommitteeButton = new javax.swing.JButton();
        clearCommitteeButton = new javax.swing.JButton();
        refreshCommitteeButton = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        committeesTable = new javax.swing.JTable();
        delegatesPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        delegateIdField = new javax.swing.JTextField();
        firstNameField = new javax.swing.JTextField();
        lastNameField = new javax.swing.JTextField();
        emailField = new javax.swing.JTextField();
        schoolField = new javax.swing.JTextField();
        dobField = new javax.swing.JTextField();
        statusComboBox = new javax.swing.JComboBox<>();
        countryComboBox = new javax.swing.JComboBox<>();
        committeeComboBox = new javax.swing.JComboBox<>();
        addButton = new javax.swing.JButton();
        updateButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        searchButton = new javax.swing.JButton();
        clearButton = new javax.swing.JButton();
        refreshButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        delegatesTable = new javax.swing.JTable();
        attendancePanel = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        delegateAttendanceComboBox = new javax.swing.JComboBox<>();
        jLabel16 = new javax.swing.JLabel();
        sessionAttendanceComboBox = new javax.swing.JComboBox<>();
        jLabel17 = new javax.swing.JLabel();
        statusAttendanceComboBox = new javax.swing.JComboBox<>();
        addAttendanceButton = new javax.swing.JButton();
        updateAttendanceButton = new javax.swing.JButton();
        deleteAttendanceButton = new javax.swing.JButton();
        searchAttendanceButton = new javax.swing.JButton();
        clearAttendanceButton = new javax.swing.JButton();
        refreshAttendanceButton = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        attendanceTable = new javax.swing.JTable();
        reportsPanel = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();
        alldelegatesReportButton = new javax.swing.JButton();
        committeeTopicsButton = new javax.swing.JButton();
        attendanceDetailsButton = new javax.swing.JButton();
        awardRecipientsButton = new javax.swing.JButton();
        delegatesPerCommitteeButton = new javax.swing.JButton();
        attendanceSummaryButton = new javax.swing.JButton();
        evaluationScoresButton = new javax.swing.JButton();
        positionPaperScoresButton = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        reportsTable = new javax.swing.JTable();
        positionPapersPanel = new javax.swing.JPanel();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        paperDelegateComboBox = new javax.swing.JComboBox<>();
        jLabel24 = new javax.swing.JLabel();
        paperCommitteeComboBox = new javax.swing.JComboBox<>();
        addPaperButton = new javax.swing.JButton();
        updatePaperButton = new javax.swing.JButton();
        deletePaperButton = new javax.swing.JButton();
        searchPaperButton = new javax.swing.JButton();
        clearPaperButton = new javax.swing.JButton();
        refreshPaperButton = new javax.swing.JButton();
        jScrollPane5 = new javax.swing.JScrollPane();
        positionPapersTable = new javax.swing.JTable();
        paperNumberField = new javax.swing.JTextField();
        submissionDateField = new javax.swing.JTextField();
        jScrollPane7 = new javax.swing.JScrollPane();
        contentTextArea = new javax.swing.JTextArea();
        scoreField = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("MUN Database Management System");

        jLabel10.setText("Committee ID");

        committeeIdField.setColumns(12);
        committeeIdField.setText("jTextField1");
        committeeIdField.addActionListener(this::committeeIdFieldActionPerformed);

        jLabel11.setText("Committee Name");

        committeeNameField.setColumns(12);
        committeeNameField.setText("jTextField1");

        jLabel12.setText("Committee Type");

        committeeTypeField.setColumns(12);
        committeeTypeField.setText("jTextField3");

        jLabel13.setText("Building Name");

        buildingNameField.setColumns(12);
        buildingNameField.setText("jTextField3");

        jLabel14.setText("Room Number");

        roomNumberField.setColumns(12);
        roomNumberField.setText("jTextField3");

        addCommitteeButton.setText("Add");
        addCommitteeButton.addActionListener(this::addCommitteeButtonActionPerformed);

        updateCommitteeButton.setText("Update");
        updateCommitteeButton.addActionListener(this::updateCommitteeButtonActionPerformed);

        deleteCommitteeButton.setText("Delete");
        deleteCommitteeButton.addActionListener(this::deleteCommitteeButtonActionPerformed);

        searchCommitteeButton.setText("Search");
        searchCommitteeButton.addActionListener(this::searchCommitteeButtonActionPerformed);

        clearCommitteeButton.setText("Clear");
        clearCommitteeButton.addActionListener(this::clearCommitteeButtonActionPerformed);

        refreshCommitteeButton.setText("Refresh");
        refreshCommitteeButton.addActionListener(this::refreshCommitteeButtonActionPerformed);

        committeesTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        committeesTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                committeesTableMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(committeesTable);

        javax.swing.GroupLayout committeesPanelLayout = new javax.swing.GroupLayout(committeesPanel);
        committeesPanel.setLayout(committeesPanelLayout);
        committeesPanelLayout.setHorizontalGroup(
            committeesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(committeesPanelLayout.createSequentialGroup()
                .addGroup(committeesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(committeesPanelLayout.createSequentialGroup()
                        .addGap(200, 200, 200)
                        .addGroup(committeesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(12, 12, 12)
                        .addGroup(committeesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(committeeNameField)
                            .addComponent(buildingNameField)
                            .addComponent(committeeTypeField)
                            .addComponent(roomNumberField)
                            .addComponent(committeeIdField))
                        .addGap(150, 150, 150))
                    .addGroup(committeesPanelLayout.createSequentialGroup()
                        .addGap(102, 102, 102)
                        .addComponent(addCommitteeButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(updateCommitteeButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(deleteCommitteeButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(searchCommitteeButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(clearCommitteeButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(refreshCommitteeButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(45, 45, 45))
                    .addGroup(committeesPanelLayout.createSequentialGroup()
                        .addGap(60, 60, 60)
                        .addComponent(jScrollPane2)))
                .addGap(60, 60, 60))
        );
        committeesPanelLayout.setVerticalGroup(
            committeesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(committeesPanelLayout.createSequentialGroup()
                .addGap(34, 34, 34)
                .addGroup(committeesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(committeeIdField, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(committeesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(committeeNameField, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(committeesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(committeeTypeField, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(committeesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(buildingNameField, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(committeesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(roomNumberField, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(37, 37, 37)
                .addGroup(committeesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addCommitteeButton)
                    .addComponent(updateCommitteeButton)
                    .addComponent(deleteCommitteeButton)
                    .addComponent(searchCommitteeButton)
                    .addComponent(clearCommitteeButton)
                    .addComponent(refreshCommitteeButton))
                .addGap(29, 29, 29)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 421, Short.MAX_VALUE)
                .addGap(60, 60, 60))
        );

        jTabbedPane1.addTab("Committees", committeesPanel);

        jLabel1.setText("Delegate ID");

        jLabel2.setText("First Name");

        jLabel3.setText("Last Name");

        jLabel4.setText("Email");

        jLabel5.setText("School");

        jLabel6.setText("Date of Birth");

        jLabel7.setText("Status");

        jLabel8.setText("Country");

        jLabel9.setText("Committee");

        delegateIdField.setColumns(12);
        delegateIdField.setText("jTextField1");

        firstNameField.setColumns(12);
        firstNameField.setText("jTextField1");

        lastNameField.setColumns(12);
        lastNameField.setText("jTextField3");

        emailField.setColumns(12);
        emailField.setText("jTextField3");

        schoolField.setColumns(12);
        schoolField.setText("jTextField3");

        dobField.setColumns(12);
        dobField.setText("jTextField3");

        statusComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        countryComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        committeeComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        addButton.setText("Add");
        addButton.addActionListener(this::addButtonActionPerformed);

        updateButton.setText("Update");
        updateButton.addActionListener(this::updateButtonActionPerformed);

        deleteButton.setText("Delete");
        deleteButton.addActionListener(this::deleteButtonActionPerformed);

        searchButton.setText("Search");
        searchButton.addActionListener(this::searchButtonActionPerformed);

        clearButton.setText("Clear");
        clearButton.addActionListener(this::clearButtonActionPerformed);

        refreshButton.setText("Refresh");
        refreshButton.addActionListener(this::refreshButtonActionPerformed);

        delegatesTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        delegatesTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                delegatesTableMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(delegatesTable);

        javax.swing.GroupLayout delegatesPanelLayout = new javax.swing.GroupLayout(delegatesPanel);
        delegatesPanel.setLayout(delegatesPanelLayout);
        delegatesPanelLayout.setHorizontalGroup(
            delegatesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(delegatesPanelLayout.createSequentialGroup()
                .addGap(183, 183, 183)
                .addGroup(delegatesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(delegatesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(delegateIdField, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(firstNameField, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                    .addComponent(lastNameField, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                    .addComponent(emailField, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                    .addComponent(dobField, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                    .addComponent(statusComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(countryComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(committeeComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(schoolField, javax.swing.GroupLayout.Alignment.TRAILING))
                .addGap(226, 226, 226))
            .addGroup(delegatesPanelLayout.createSequentialGroup()
                .addGap(96, 96, 96)
                .addComponent(addButton, javax.swing.GroupLayout.DEFAULT_SIZE, 88, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(updateButton, javax.swing.GroupLayout.DEFAULT_SIZE, 88, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(deleteButton, javax.swing.GroupLayout.DEFAULT_SIZE, 88, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(searchButton, javax.swing.GroupLayout.DEFAULT_SIZE, 89, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(clearButton, javax.swing.GroupLayout.DEFAULT_SIZE, 89, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(refreshButton, javax.swing.GroupLayout.DEFAULT_SIZE, 89, Short.MAX_VALUE)
                .addGap(176, 176, 176))
            .addGroup(delegatesPanelLayout.createSequentialGroup()
                .addGap(98, 98, 98)
                .addComponent(jScrollPane1)
                .addGap(98, 98, 98))
        );
        delegatesPanelLayout.setVerticalGroup(
            delegatesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(delegatesPanelLayout.createSequentialGroup()
                .addGap(34, 34, 34)
                .addGroup(delegatesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1)
                    .addComponent(delegateIdField, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(delegatesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(firstNameField, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(delegatesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(lastNameField, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(delegatesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(emailField, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(delegatesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(schoolField, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(delegatesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(dobField, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(delegatesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(statusComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(delegatesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(countryComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(delegatesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(committeeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(28, 28, 28)
                .addGroup(delegatesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addButton)
                    .addComponent(updateButton)
                    .addComponent(deleteButton)
                    .addComponent(searchButton)
                    .addComponent(clearButton)
                    .addComponent(refreshButton))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 349, Short.MAX_VALUE)
                .addGap(60, 60, 60))
        );

        jTabbedPane1.addTab("Delegates", delegatesPanel);

        jLabel15.setText("Delegate");

        delegateAttendanceComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel16.setText("Session");

        sessionAttendanceComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel17.setText("Status");

        statusAttendanceComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        addAttendanceButton.setText("Add");
        addAttendanceButton.addActionListener(this::addAttendanceButtonActionPerformed);

        updateAttendanceButton.setText("Update");
        updateAttendanceButton.addActionListener(this::updateAttendanceButtonActionPerformed);

        deleteAttendanceButton.setText("Delete");
        deleteAttendanceButton.addActionListener(this::deleteAttendanceButtonActionPerformed);

        searchAttendanceButton.setText("Search");
        searchAttendanceButton.addActionListener(this::searchAttendanceButtonActionPerformed);

        clearAttendanceButton.setText("Clear");
        clearAttendanceButton.addActionListener(this::clearAttendanceButtonActionPerformed);

        refreshAttendanceButton.setText("Refresh");
        refreshAttendanceButton.addActionListener(this::refreshAttendanceButtonActionPerformed);

        attendanceTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        attendanceTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                attendanceTableMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(attendanceTable);

        javax.swing.GroupLayout attendancePanelLayout = new javax.swing.GroupLayout(attendancePanel);
        attendancePanel.setLayout(attendancePanelLayout);
        attendancePanelLayout.setHorizontalGroup(
            attendancePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(attendancePanelLayout.createSequentialGroup()
                .addGroup(attendancePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(attendancePanelLayout.createSequentialGroup()
                        .addGap(257, 257, 257)
                        .addGroup(attendancePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel15, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel16, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(attendancePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(delegateAttendanceComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(sessionAttendanceComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(statusAttendanceComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(256, 256, 256))
                    .addGroup(attendancePanelLayout.createSequentialGroup()
                        .addGap(113, 113, 113)
                        .addComponent(addAttendanceButton, javax.swing.GroupLayout.DEFAULT_SIZE, 97, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(updateAttendanceButton, javax.swing.GroupLayout.DEFAULT_SIZE, 97, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(deleteAttendanceButton, javax.swing.GroupLayout.DEFAULT_SIZE, 97, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(searchAttendanceButton, javax.swing.GroupLayout.DEFAULT_SIZE, 98, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(clearAttendanceButton, javax.swing.GroupLayout.DEFAULT_SIZE, 98, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(refreshAttendanceButton, javax.swing.GroupLayout.DEFAULT_SIZE, 98, Short.MAX_VALUE)
                        .addGap(45, 45, 45))
                    .addGroup(attendancePanelLayout.createSequentialGroup()
                        .addGap(60, 60, 60)
                        .addComponent(jScrollPane3)))
                .addGap(60, 60, 60))
        );
        attendancePanelLayout.setVerticalGroup(
            attendancePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(attendancePanelLayout.createSequentialGroup()
                .addGap(34, 34, 34)
                .addGroup(attendancePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(delegateAttendanceComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(attendancePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16)
                    .addComponent(sessionAttendanceComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(attendancePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel17)
                    .addComponent(statusAttendanceComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(attendancePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addAttendanceButton)
                    .addComponent(updateAttendanceButton)
                    .addComponent(deleteAttendanceButton)
                    .addComponent(searchAttendanceButton)
                    .addComponent(clearAttendanceButton)
                    .addComponent(refreshAttendanceButton))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane3)
                .addGap(60, 60, 60))
        );

        jTabbedPane1.addTab("Attendance", attendancePanel);

        jLabel18.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel18.setText("Report Results");

        alldelegatesReportButton.setText("All Delegates");
        alldelegatesReportButton.addActionListener(this::alldelegatesReportButtonActionPerformed);

        committeeTopicsButton.setText("Committee Topics");
        committeeTopicsButton.addActionListener(this::committeeTopicsButtonActionPerformed);

        attendanceDetailsButton.setText("Attendance Details");
        attendanceDetailsButton.addActionListener(this::attendanceDetailsButtonActionPerformed);

        awardRecipientsButton.setText("Award Recipients");
        awardRecipientsButton.addActionListener(this::awardRecipientsButtonActionPerformed);

        delegatesPerCommitteeButton.setText("Delegates Per Committee");
        delegatesPerCommitteeButton.addActionListener(this::delegatesPerCommitteeButtonActionPerformed);

        attendanceSummaryButton.setText("Attendance Summary");
        attendanceSummaryButton.addActionListener(this::attendanceSummaryButtonActionPerformed);

        evaluationScoresButton.setText("Evaluation Scores");
        evaluationScoresButton.addActionListener(this::evaluationScoresButtonActionPerformed);

        positionPaperScoresButton.setText("Position Paper Scores");
        positionPaperScoresButton.addActionListener(this::positionPaperScoresButtonActionPerformed);

        reportsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane4.setViewportView(reportsTable);

        javax.swing.GroupLayout reportsPanelLayout = new javax.swing.GroupLayout(reportsPanel);
        reportsPanel.setLayout(reportsPanelLayout);
        reportsPanelLayout.setHorizontalGroup(
            reportsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, reportsPanelLayout.createSequentialGroup()
                .addGroup(reportsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(reportsPanelLayout.createSequentialGroup()
                        .addGap(60, 60, 60)
                        .addComponent(jScrollPane4))
                    .addGroup(reportsPanelLayout.createSequentialGroup()
                        .addGap(129, 129, 129)
                        .addGroup(reportsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(attendanceDetailsButton, javax.swing.GroupLayout.DEFAULT_SIZE, 186, Short.MAX_VALUE)
                            .addComponent(alldelegatesReportButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(evaluationScoresButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(30, 30, 30)
                        .addGroup(reportsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel18, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(attendanceSummaryButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(delegatesPerCommitteeButton, javax.swing.GroupLayout.DEFAULT_SIZE, 224, Short.MAX_VALUE))
                        .addGap(30, 30, 30)
                        .addGroup(reportsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(committeeTopicsButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(awardRecipientsButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(positionPaperScoresButton, javax.swing.GroupLayout.DEFAULT_SIZE, 204, Short.MAX_VALUE))))
                .addGap(60, 60, 60))
        );
        reportsPanelLayout.setVerticalGroup(
            reportsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(reportsPanelLayout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addComponent(jLabel18)
                .addGroup(reportsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(reportsPanelLayout.createSequentialGroup()
                        .addGap(36, 36, 36)
                        .addGroup(reportsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(alldelegatesReportButton)
                            .addComponent(committeeTopicsButton)
                            .addComponent(delegatesPerCommitteeButton)))
                    .addGroup(reportsPanelLayout.createSequentialGroup()
                        .addGap(90, 90, 90)
                        .addGroup(reportsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(awardRecipientsButton)
                            .addComponent(attendanceDetailsButton)
                            .addComponent(attendanceSummaryButton))))
                .addGap(30, 30, 30)
                .addGroup(reportsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(evaluationScoresButton)
                    .addComponent(positionPaperScoresButton))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane4)
                .addGap(60, 60, 60))
        );

        jTabbedPane1.addTab("Reports", reportsPanel);

        jLabel19.setText("Paper Number");

        jLabel20.setText("Submission Date");

        jLabel21.setText("Content");

        jLabel22.setText("Score");

        jLabel23.setText("Delegate");

        paperDelegateComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel24.setText("Committee");

        paperCommitteeComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        addPaperButton.setText("Add");
        addPaperButton.addActionListener(this::addPaperButtonActionPerformed);

        updatePaperButton.setText("Update");
        updatePaperButton.addActionListener(this::updatePaperButtonActionPerformed);

        deletePaperButton.setText("Delete");
        deletePaperButton.addActionListener(this::deletePaperButtonActionPerformed);

        searchPaperButton.setText("Search");
        searchPaperButton.addActionListener(this::searchPaperButtonActionPerformed);

        clearPaperButton.setText("Clear");
        clearPaperButton.addActionListener(this::clearPaperButtonActionPerformed);

        refreshPaperButton.setText("Refresh");
        refreshPaperButton.addActionListener(this::refreshPaperButtonActionPerformed);

        positionPapersTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        positionPapersTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                positionPapersTableMouseClicked(evt);
            }
        });
        jScrollPane5.setViewportView(positionPapersTable);

        paperNumberField.setColumns(12);
        paperNumberField.setText("jTextField1");

        submissionDateField.setColumns(12);
        submissionDateField.setText("jTextField1");

        contentTextArea.setColumns(12);
        contentTextArea.setRows(10);
        jScrollPane7.setViewportView(contentTextArea);

        scoreField.setColumns(12);
        scoreField.setText("jTextField3");

        javax.swing.GroupLayout positionPapersPanelLayout = new javax.swing.GroupLayout(positionPapersPanel);
        positionPapersPanel.setLayout(positionPapersPanelLayout);
        positionPapersPanelLayout.setHorizontalGroup(
            positionPapersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(positionPapersPanelLayout.createSequentialGroup()
                .addGroup(positionPapersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(positionPapersPanelLayout.createSequentialGroup()
                        .addGap(60, 60, 60)
                        .addComponent(jScrollPane5)
                        .addGap(3, 3, 3))
                    .addGroup(positionPapersPanelLayout.createSequentialGroup()
                        .addGap(235, 235, 235)
                        .addGroup(positionPapersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel21, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel22, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel20, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel19, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel23, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel24, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(positionPapersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(paperCommitteeComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(paperDelegateComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(positionPapersPanelLayout.createSequentialGroup()
                                .addComponent(jScrollPane7)
                                .addGap(2, 2, 2))
                            .addComponent(paperNumberField)
                            .addComponent(submissionDateField)
                            .addComponent(scoreField))
                        .addGap(182, 182, 182))
                    .addGroup(positionPapersPanelLayout.createSequentialGroup()
                        .addGap(130, 130, 130)
                        .addComponent(addPaperButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(updatePaperButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(deletePaperButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(searchPaperButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(clearPaperButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(refreshPaperButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(92, 92, 92)))
                .addGap(60, 60, 60))
        );
        positionPapersPanelLayout.setVerticalGroup(
            positionPapersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(positionPapersPanelLayout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addGroup(positionPapersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel19)
                    .addComponent(paperNumberField, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(13, 13, 13)
                .addGroup(positionPapersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel20)
                    .addComponent(submissionDateField, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(positionPapersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel21, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(positionPapersPanelLayout.createSequentialGroup()
                        .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(positionPapersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel22)
                    .addComponent(scoreField, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(positionPapersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel23)
                    .addComponent(paperDelegateComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(positionPapersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel24)
                    .addComponent(paperCommitteeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(38, 38, 38)
                .addGroup(positionPapersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addPaperButton)
                    .addComponent(updatePaperButton)
                    .addComponent(deletePaperButton)
                    .addComponent(searchPaperButton)
                    .addComponent(clearPaperButton)
                    .addComponent(refreshPaperButton))
                .addGap(30, 30, 30)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 368, Short.MAX_VALUE)
                .addGap(60, 60, 60))
        );

        jTabbedPane1.addTab("Position Papers", positionPapersPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 748, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void updateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateButtonActionPerformed
        // TODO add your handling code here:
        
    String delegateIdText = delegateIdField.getText().trim();
    String firstName = firstNameField.getText().trim();
    String lastName = lastNameField.getText().trim();
    String email = emailField.getText().trim();
    String school = schoolField.getText().trim();
    String dobText = dobField.getText().trim();

    String status = (String) statusComboBox.getSelectedItem();
    String countryName = (String) countryComboBox.getSelectedItem();
    String committeeName = (String) committeeComboBox.getSelectedItem();

    if (delegateIdText.isEmpty()
            || firstName.isEmpty()
            || lastName.isEmpty()
            || email.isEmpty()
            || dobText.isEmpty()) {

        JOptionPane.showMessageDialog(
                this,
                "Delegate ID, first name, last name, email, and date of birth are required.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE
        );

        return;
    }

    int delegateId;

    try {
        delegateId = Integer.parseInt(delegateIdText);

    } catch (NumberFormatException e) {

        JOptionPane.showMessageDialog(
                this,
                "Delegate ID must be a number.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE
        );

        return;
    }
    if (!isValidDate(dobText)) {

    JOptionPane.showMessageDialog(
            this,
            "Date of birth must use DD-MON-YYYY, for example 15-MAR-2005.",
            "Validation Error",
            JOptionPane.WARNING_MESSAGE
    );

    return;
}
    if (!isValidEmail(email)) {

    JOptionPane.showMessageDialog(
            this,
            "Enter a valid email address.",
            "Validation Error",
            JOptionPane.WARNING_MESSAGE
    );

    return;
}
    String sql = """
            UPDATE delegate
            SET f_name = ?,
                l_name = ?,
                email = ?,
                school = ?,
                dob = TO_DATE(?, 'DD-MON-YYYY'),
                regi_status = ?,
                country_id = (
                    SELECT country_id
                    FROM country
                    WHERE country_name = ?
                ),
                com_id = (
                    SELECT com_id
                    FROM committee
                    WHERE com_name = ?
                )
            WHERE d_id = ?
            """;

    try (
        Connection con = DriverManager.getConnection(
                MUNDatabaseGUI.DBURL,
                MUNDatabaseGUI.DBUSER,
                MUNDatabaseGUI.DBPASS
        );
        PreparedStatement statement = con.prepareStatement(sql)
    ) {

        statement.setString(1, firstName);
        statement.setString(2, lastName);
        statement.setString(3, email);

        if (school.isEmpty()) {
            statement.setNull(4, java.sql.Types.VARCHAR);
        } else {
            statement.setString(4, school);
        }

        statement.setString(5, dobText);
        statement.setString(6, status);
        statement.setString(7, countryName);
        statement.setString(8, committeeName);
        statement.setInt(9, delegateId);

        int rowsUpdated = statement.executeUpdate();

        if (rowsUpdated == 1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Delegate updated successfully.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
            );

            clearDelegateFields();
            loadDelegates();

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    "No delegate was found with ID " + delegateId + ".",
                    "Not Found",
                    JOptionPane.WARNING_MESSAGE
            );
        }

    } catch (SQLException e) {

        JOptionPane.showMessageDialog(
                this,
                """
                Could not update delegate.
                
                Oracle error: """ + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
    }//GEN-LAST:event_updateButtonActionPerformed

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
        // TODO add your handling code here:
            String delegateIdText = delegateIdField.getText().trim();

    if (delegateIdText.isEmpty()) {
        JOptionPane.showMessageDialog(
                this,
                "Select a delegate or enter a Delegate ID first.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE
        );
        return;
    }

    int delegateId;

    try {
        delegateId = Integer.parseInt(delegateIdText);

    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(
                this,
                "Delegate ID must be a number.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE
        );
        return;
    }

    int confirmation = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete delegate " + delegateId + "?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
    );

    if (confirmation != JOptionPane.YES_OPTION) {
        return;
    }

    String sql = """
            DELETE FROM delegate
            WHERE d_id = ?
            """;

    try (
        Connection con = DriverManager.getConnection(
                MUNDatabaseGUI.DBURL,
                MUNDatabaseGUI.DBUSER,
                MUNDatabaseGUI.DBPASS
        );
        PreparedStatement statement = con.prepareStatement(sql)
    ) {

        statement.setInt(1, delegateId);

        int rowsDeleted = statement.executeUpdate();

        if (rowsDeleted == 1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Delegate deleted successfully.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
            );

            clearDelegateFields();
            loadDelegates();

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    "No delegate was found with ID " + delegateId + ".",
                    "Not Found",
                    JOptionPane.WARNING_MESSAGE
            );
        }

    } catch (SQLException e) {

        JOptionPane.showMessageDialog(
                this,
                """
                Could not delete delegate.
                
                This delegate may still be referenced by attendance, position papers, awards, workshops, or evaluations.
                
                Oracle error: """ + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
    }//GEN-LAST:event_deleteButtonActionPerformed

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        // TODO add your handling code here:
        String delegateIdText = delegateIdField.getText().trim();
    String firstName = firstNameField.getText().trim();
    String lastName = lastNameField.getText().trim();
    String email = emailField.getText().trim();
    String school = schoolField.getText().trim();
    String dobText = dobField.getText().trim();

    String status = (String) statusComboBox.getSelectedItem();
    String countryName = (String) countryComboBox.getSelectedItem();
    String committeeName = (String) committeeComboBox.getSelectedItem();

    if (delegateIdText.isEmpty()
            || firstName.isEmpty()
            || lastName.isEmpty()
            || email.isEmpty()
            || dobText.isEmpty()) {

        JOptionPane.showMessageDialog(
                this,
                "Delegate ID, first name, last name, email, and date of birth are required.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE
        );

        return;
    }

    int delegateId;

    try {
        delegateId = Integer.parseInt(delegateIdText);

    } catch (NumberFormatException e) {

        JOptionPane.showMessageDialog(
                this,
                "Delegate ID must be a number.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE
        );

        return;
    }
    if (!isValidDate(dobText)) {

    JOptionPane.showMessageDialog(
            this,
            "Date of birth must use DD-MON-YYYY, for example 15-MAR-2005.",
            "Validation Error",
            JOptionPane.WARNING_MESSAGE
    );

    return;
}
    if (!isValidEmail(email)) {

    JOptionPane.showMessageDialog(
            this,
            "Enter a valid email address.",
            "Validation Error",
            JOptionPane.WARNING_MESSAGE
    );

    return;
}
    if (delegateId < 0 || delegateId > 9999) {

    JOptionPane.showMessageDialog(
            this,
            "Delegate ID must be between 0 and 9999.",
            "Validation Error",
            JOptionPane.WARNING_MESSAGE
    );

    return;
}
    String sql = """
            INSERT INTO delegate
            (
                d_id,
                f_name,
                l_name,
                email,
                school,
                dob,
                regi_status,
                country_id,
                com_id
            )
            VALUES
            (
                ?,
                ?,
                ?,
                ?,
                ?,
                TO_DATE(?, 'DD-MON-YYYY'),
                ?,
                (SELECT country_id
                 FROM country
                 WHERE country_name = ?),
                (SELECT com_id
                 FROM committee
                 WHERE com_name = ?)
            )
            """;

    try (
        Connection con = DriverManager.getConnection(
                MUNDatabaseGUI.DBURL,
                MUNDatabaseGUI.DBUSER,
                MUNDatabaseGUI.DBPASS
        );
        PreparedStatement statement = con.prepareStatement(sql)
    ) {

        statement.setInt(1, delegateId);
        statement.setString(2, firstName);
        statement.setString(3, lastName);
        statement.setString(4, email);

        if (school.isEmpty()) {
            statement.setNull(5, java.sql.Types.VARCHAR);
        } else {
            statement.setString(5, school);
        }

        statement.setString(6, dobText);
        statement.setString(7, status);
        statement.setString(8, countryName);
        statement.setString(9, committeeName);

        int rowsInserted = statement.executeUpdate();

        if (rowsInserted == 1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Delegate added successfully.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
            );

            clearDelegateFields();
            loadDelegates();
        }

    } catch (SQLException e) {

        JOptionPane.showMessageDialog(
                this,
                "Could not add delegate.\n\n"
                + "Oracle error: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
    }//GEN-LAST:event_addButtonActionPerformed

    private void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearButtonActionPerformed
        // TODO add your handling code here:
        clearDelegateFields();
    }//GEN-LAST:event_clearButtonActionPerformed

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        // TODO add your handling code here:
        loadDelegates();
    }//GEN-LAST:event_refreshButtonActionPerformed

    private void delegatesTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_delegatesTableMouseClicked
        // TODO add your handling code here:
        
    int selectedRow = delegatesTable.getSelectedRow();

    if (selectedRow == -1) {
        return;
    }

    delegateIdField.setText(
            delegatesTable.getValueAt(selectedRow, 0).toString()
    );

    firstNameField.setText(
            delegatesTable.getValueAt(selectedRow, 1).toString()
    );

    lastNameField.setText(
            delegatesTable.getValueAt(selectedRow, 2).toString()
    );

    emailField.setText(
            delegatesTable.getValueAt(selectedRow, 3).toString()
    );

    Object schoolValue = delegatesTable.getValueAt(selectedRow, 4);

    schoolField.setText(
            schoolValue == null ? "" : schoolValue.toString()
    );

    dobField.setText(
            delegatesTable.getValueAt(selectedRow, 5).toString()
    );

    statusComboBox.setSelectedItem(
            delegatesTable.getValueAt(selectedRow, 6).toString()
    );

    countryComboBox.setSelectedItem(
            delegatesTable.getValueAt(selectedRow, 7).toString()
    );

    committeeComboBox.setSelectedItem(
            delegatesTable.getValueAt(selectedRow, 8).toString()
    );
    delegateIdField.setEditable(false);
    }//GEN-LAST:event_delegatesTableMouseClicked

    private void searchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed
        // TODO add your handling code here:
        
    String delegateIdText = delegateIdField.getText().trim();

    if (delegateIdText.isEmpty()) {
        loadDelegates();
        return;
    }

    int delegateId;

    try {
        delegateId = Integer.parseInt(delegateIdText);

    } catch (NumberFormatException e) {

        JOptionPane.showMessageDialog(
                this,
                "Delegate ID must be a number.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE
        );

        return;
    }

    String sql = """
            SELECT d.d_id,
                   d.f_name,
                   d.l_name,
                   d.email,
                   d.school,
                   TO_CHAR(d.dob, 'DD-MON-YYYY') AS dob,
                   d.regi_status,
                   c.country_name,
                   cm.com_name
            FROM delegate d
            JOIN country c
                ON d.country_id = c.country_id
            JOIN committee cm
                ON d.com_id = cm.com_id
            WHERE d.d_id = ?
            """;

    String[] columnNames = {
        "Delegate ID",
        "First Name",
        "Last Name",
        "Email",
        "School",
        "Date of Birth",
        "Status",
        "Country",
        "Committee"
    };

    DefaultTableModel model = new DefaultTableModel(columnNames, 0);

    try (
        Connection con = DriverManager.getConnection(
                MUNDatabaseGUI.DBURL,
                MUNDatabaseGUI.DBUSER,
                MUNDatabaseGUI.DBPASS
        );
        PreparedStatement statement = con.prepareStatement(sql)
    ) {

        statement.setInt(1, delegateId);

        ResultSet resultSet = statement.executeQuery();

        while (resultSet.next()) {

            model.addRow(new Object[]{
                resultSet.getInt("d_id"),
                resultSet.getString("f_name"),
                resultSet.getString("l_name"),
                resultSet.getString("email"),
                resultSet.getString("school"),
                resultSet.getString("dob"),
                resultSet.getString("regi_status"),
                resultSet.getString("country_name"),
                resultSet.getString("com_name")
            });
        }

        delegatesTable.setModel(model);

        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "No delegate found with ID " + delegateId + ".",
                    "Search",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }

    } catch (SQLException e) {

        JOptionPane.showMessageDialog(
                this,
                "Could not search delegate.\n\n"
                + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
    }//GEN-LAST:event_searchButtonActionPerformed

    private void addCommitteeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addCommitteeButtonActionPerformed
        // TODO add your handling code here:
            String committeeIdText = committeeIdField.getText().trim();
    String committeeName = committeeNameField.getText().trim();
    String committeeType = committeeTypeField.getText().trim();
    String buildingName = buildingNameField.getText().trim();
    String roomNumber = roomNumberField.getText().trim();

    if (committeeIdText.isEmpty()
            || committeeName.isEmpty()
            || committeeType.isEmpty()
            || buildingName.isEmpty()
            || roomNumber.isEmpty()) {

        JOptionPane.showMessageDialog(
                this,
                "Please fill in all committee fields.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE
        );
        return;
    }

    int committeeId;

    try {
        committeeId = Integer.parseInt(committeeIdText);

    } catch (NumberFormatException e) {

        JOptionPane.showMessageDialog(
                this,
                "Committee ID must be a number.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE
        );
        return;
    }
    if (committeeId < 0 || committeeId > 9999) {
    JOptionPane.showMessageDialog(
            this,
            "Committee ID must be between 0 and 9999.",
            "Validation Error",
            JOptionPane.WARNING_MESSAGE
    );
    return;
}
    String sql = """
            INSERT INTO committee
            (
                com_id,
                com_name,
                com_type,
                building_name,
                room_no
            )
            VALUES
            (
                ?, ?, ?, ?, ?
            )
            """;

    try (
        Connection con = DriverManager.getConnection(
                MUNDatabaseGUI.DBURL,
                MUNDatabaseGUI.DBUSER,
                MUNDatabaseGUI.DBPASS
        );
        PreparedStatement statement = con.prepareStatement(sql)
    ) {

        statement.setInt(1, committeeId);
        statement.setString(2, committeeName);
        statement.setString(3, committeeType);
        statement.setString(4, buildingName);
        statement.setString(5, roomNumber);

        int rowsInserted = statement.executeUpdate();

        if (rowsInserted == 1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Committee added successfully.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
            );

            clearCommitteeFields();
            loadCommitteeTable();

            // Refresh the Delegate tab's Committee combo box
            loadCommittees();
        }

    } catch (SQLException e) {

        JOptionPane.showMessageDialog(
                this,
                """
                Could not add committee.
                
                Oracle error: """ + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
    }//GEN-LAST:event_addCommitteeButtonActionPerformed

    private void updateCommitteeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateCommitteeButtonActionPerformed
        // TODO add your handling code here:
        
    String committeeIdText = committeeIdField.getText().trim();
    String committeeName = committeeNameField.getText().trim();
    String committeeType = committeeTypeField.getText().trim();
    String buildingName = buildingNameField.getText().trim();
    String roomNumber = roomNumberField.getText().trim();

    if (committeeIdText.isEmpty()
            || committeeName.isEmpty()
            || committeeType.isEmpty()
            || buildingName.isEmpty()
            || roomNumber.isEmpty()) {

        JOptionPane.showMessageDialog(
                this,
                "Please fill in all committee fields.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE
        );
        return;
    }

    int committeeId;

    try {
        committeeId = Integer.parseInt(committeeIdText);
    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(
                this,
                "Committee ID must be a number.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE
        );
        return;
    }

    String sql = """
            UPDATE committee
            SET com_name = ?,
                com_type = ?,
                building_name = ?,
                room_no = ?
            WHERE com_id = ?
            """;

    try (
        Connection con = DriverManager.getConnection(
                MUNDatabaseGUI.DBURL,
                MUNDatabaseGUI.DBUSER,
                MUNDatabaseGUI.DBPASS
        );
        PreparedStatement statement = con.prepareStatement(sql)
    ) {

        statement.setString(1, committeeName);
        statement.setString(2, committeeType);
        statement.setString(3, buildingName);
        statement.setString(4, roomNumber);
        statement.setInt(5, committeeId);

        if (statement.executeUpdate() == 1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Committee updated successfully."
            );

            clearCommitteeFields();
            loadCommitteeTable();
            loadCommittees();
        }

    } catch (SQLException e) {

        JOptionPane.showMessageDialog(
                this,
                e.getMessage()
        );
    }
    }//GEN-LAST:event_updateCommitteeButtonActionPerformed

    private void deleteCommitteeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteCommitteeButtonActionPerformed
        // TODO add your handling code here:
            String committeeIdText = committeeIdField.getText().trim();

    if (committeeIdText.isEmpty()) {
        JOptionPane.showMessageDialog(
                this,
                "Select a committee first."
        );
        return;
    }

    int committeeId = Integer.parseInt(committeeIdText);

    int option = JOptionPane.showConfirmDialog(
            this,
            "Delete this committee?",
            "Confirm",
            JOptionPane.YES_NO_OPTION
    );

    if (option != JOptionPane.YES_OPTION) {
        return;
    }

    String sql = """
            DELETE FROM committee
            WHERE com_id = ?
            """;

    try (
        Connection con = DriverManager.getConnection(
                MUNDatabaseGUI.DBURL,
                MUNDatabaseGUI.DBUSER,
                MUNDatabaseGUI.DBPASS
        );
        PreparedStatement statement = con.prepareStatement(sql)
    ) {

        statement.setInt(1, committeeId);

        statement.executeUpdate();

        JOptionPane.showMessageDialog(
                this,
                "Committee deleted."
        );

        clearCommitteeFields();
        loadCommitteeTable();
        loadCommittees();

    } catch (SQLException e) {

        JOptionPane.showMessageDialog(
                this,
                "Cannot delete committee.\n\n"
                + e.getMessage()
        );
    }
    }//GEN-LAST:event_deleteCommitteeButtonActionPerformed

    private void searchCommitteeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchCommitteeButtonActionPerformed
        // TODO add your handling code here:
            String committeeIdText = committeeIdField.getText().trim();

    if (committeeIdText.isEmpty()) {
        loadCommitteeTable();
        return;
    }

    int committeeId;

    try {
        committeeId = Integer.parseInt(committeeIdText);
    } catch (NumberFormatException e) {

        JOptionPane.showMessageDialog(
                this,
                "Committee ID must be numeric."
        );
        return;
    }

    String sql = """
            SELECT *
            FROM committee
            WHERE com_id = ?
            """;

    DefaultTableModel model = new DefaultTableModel(
            new String[]{
                "Committee ID",
                "Committee Name",
                "Committee Type",
                "Building",
                "Room"
            },
            0
    );

    try (
        Connection con = DriverManager.getConnection(
                MUNDatabaseGUI.DBURL,
                MUNDatabaseGUI.DBUSER,
                MUNDatabaseGUI.DBPASS
        );
        PreparedStatement statement = con.prepareStatement(sql)
    ) {

        statement.setInt(1, committeeId);

        ResultSet rs = statement.executeQuery();

        while (rs.next()) {

            model.addRow(new Object[]{
                rs.getInt("com_id"),
                rs.getString("com_name"),
                rs.getString("com_type"),
                rs.getString("building_name"),
                rs.getString("room_no")
            });
        }

        committeesTable.setModel(model);

    } catch (SQLException e) {

        JOptionPane.showMessageDialog(
                this,
                e.getMessage()
        );
    }
    }//GEN-LAST:event_searchCommitteeButtonActionPerformed

    private void clearCommitteeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearCommitteeButtonActionPerformed
        // TODO add your handling code here:
        clearCommitteeFields();
    }//GEN-LAST:event_clearCommitteeButtonActionPerformed

    private void refreshCommitteeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshCommitteeButtonActionPerformed
        // TODO add your handling code here:
        loadCommitteeTable();
    }//GEN-LAST:event_refreshCommitteeButtonActionPerformed

    private void committeesTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_committeesTableMouseClicked
        // TODO add your handling code here:
            int selectedRow = committeesTable.getSelectedRow();

    if (selectedRow == -1) {
        return;
    }

    committeeIdField.setText(
            committeesTable.getValueAt(selectedRow, 0).toString()
    );

    committeeNameField.setText(
            committeesTable.getValueAt(selectedRow, 1).toString()
    );

    committeeTypeField.setText(
            committeesTable.getValueAt(selectedRow, 2).toString()
    );

    buildingNameField.setText(
            committeesTable.getValueAt(selectedRow, 3).toString()
    );

    roomNumberField.setText(
            committeesTable.getValueAt(selectedRow, 4).toString()
    );
    committeeIdField.setEditable(false);
    }//GEN-LAST:event_committeesTableMouseClicked

    private void addAttendanceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addAttendanceButtonActionPerformed
        // TODO add your handling code here:
            String delegateItem
            = (String) delegateAttendanceComboBox.getSelectedItem();

    String sessionItem
            = (String) sessionAttendanceComboBox.getSelectedItem();

    String status
            = (String) statusAttendanceComboBox.getSelectedItem();

    if (delegateItem == null
            || sessionItem == null
            || status == null) {

        JOptionPane.showMessageDialog(
                this,
                "Please select a delegate, session, and attendance status.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE
        );

        return;
    }

    int delegateId;
    int sessionId;

    try {
        delegateId = Integer.parseInt(
                delegateItem.split(" - ")[0].trim()
        );

        sessionId = Integer.parseInt(
                sessionItem.split(" - ")[0].trim()
        );

    } catch (NumberFormatException e) {

        JOptionPane.showMessageDialog(
                this,
                "Could not read the delegate or session ID.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE
        );

        return;
    }

    String sql = """
            INSERT INTO attendance
            (
                d_id,
                session_id,
                status
            )
            VALUES (?, ?, ?)
            """;

    try (
        Connection con = DriverManager.getConnection(
                MUNDatabaseGUI.DBURL,
                MUNDatabaseGUI.DBUSER,
                MUNDatabaseGUI.DBPASS
        );
        PreparedStatement statement = con.prepareStatement(sql)
    ) {

        statement.setInt(1, delegateId);
        statement.setInt(2, sessionId);
        statement.setString(3, status);

        int rowsInserted = statement.executeUpdate();

        if (rowsInserted == 1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Attendance recorded successfully.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
            );

            loadAttendanceTable();
        }

    } catch (SQLException e) {

        JOptionPane.showMessageDialog(
                this,
                """
                Could not record attendance.
                
                The selected delegate may already have an attendance record for this session.
                
                Oracle error: """ + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
    }//GEN-LAST:event_addAttendanceButtonActionPerformed

    private void updateAttendanceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateAttendanceButtonActionPerformed
        // TODO add your handling code here:
            String delegateItem
            = (String) delegateAttendanceComboBox.getSelectedItem();

    String sessionItem
            = (String) sessionAttendanceComboBox.getSelectedItem();

    String status
            = (String) statusAttendanceComboBox.getSelectedItem();

    if (delegateItem == null
            || sessionItem == null
            || status == null) {

        JOptionPane.showMessageDialog(
                this,
                "Please select a delegate, session, and status.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE
        );

        return;
    }

    int delegateId;
    int sessionId;

    try {
        delegateId = Integer.parseInt(
                delegateItem.split(" - ")[0].trim()
        );

        sessionId = Integer.parseInt(
                sessionItem.split(" - ")[0].trim()
        );

    } catch (NumberFormatException e) {

        JOptionPane.showMessageDialog(
                this,
                "Could not read the delegate or session ID.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE
        );

        return;
    }

    String sql = """
            UPDATE attendance
            SET status = ?
            WHERE d_id = ?
              AND session_id = ?
            """;

    try (
        Connection con = DriverManager.getConnection(
                MUNDatabaseGUI.DBURL,
                MUNDatabaseGUI.DBUSER,
                MUNDatabaseGUI.DBPASS
        );
        PreparedStatement statement = con.prepareStatement(sql)
    ) {

        statement.setString(1, status);
        statement.setInt(2, delegateId);
        statement.setInt(3, sessionId);

        int rowsUpdated = statement.executeUpdate();

        if (rowsUpdated == 1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Attendance updated successfully.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
            );

            loadAttendanceTable();

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    "No matching attendance record was found.",
                    "Not Found",
                    JOptionPane.WARNING_MESSAGE
            );
        }

    } catch (SQLException e) {

        JOptionPane.showMessageDialog(
                this,
                """
                Could not update attendance.
                
                Oracle error: """ + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
    }//GEN-LAST:event_updateAttendanceButtonActionPerformed

    private void deleteAttendanceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteAttendanceButtonActionPerformed
        // TODO add your handling code here:
            String delegateItem
            = (String) delegateAttendanceComboBox.getSelectedItem();

    String sessionItem
            = (String) sessionAttendanceComboBox.getSelectedItem();

    if (delegateItem == null || sessionItem == null) {
        JOptionPane.showMessageDialog(
                this,
                "Please select an attendance record first.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE
        );
        return;
    }

    int delegateId;
    int sessionId;

    try {
        delegateId = Integer.parseInt(
                delegateItem.split(" - ")[0].trim()
        );

        sessionId = Integer.parseInt(
                sessionItem.split(" - ")[0].trim()
        );

    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(
                this,
                "Could not read the delegate or session ID.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE
        );
        return;
    }

    int option = JOptionPane.showConfirmDialog(
            this,
            "Delete this attendance record?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
    );

    if (option != JOptionPane.YES_OPTION) {
        return;
    }

    String sql = """
            DELETE FROM attendance
            WHERE d_id = ?
              AND session_id = ?
            """;

    try (
        Connection con = DriverManager.getConnection(
                MUNDatabaseGUI.DBURL,
                MUNDatabaseGUI.DBUSER,
                MUNDatabaseGUI.DBPASS
        );
        PreparedStatement statement = con.prepareStatement(sql)
    ) {

        statement.setInt(1, delegateId);
        statement.setInt(2, sessionId);

        int rowsDeleted = statement.executeUpdate();

        if (rowsDeleted == 1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Attendance record deleted successfully.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
            );

            loadAttendanceTable();

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    "No matching attendance record was found.",
                    "Not Found",
                    JOptionPane.WARNING_MESSAGE
            );
        }

    } catch (SQLException e) {

        JOptionPane.showMessageDialog(
                this,
                "Could not delete attendance.\n\n"
                + "Oracle error: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
    }//GEN-LAST:event_deleteAttendanceButtonActionPerformed

    private void searchAttendanceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchAttendanceButtonActionPerformed
        // TODO add your handling code here:
            String delegateItem
            = (String) delegateAttendanceComboBox.getSelectedItem();

    String sessionItem
            = (String) sessionAttendanceComboBox.getSelectedItem();

    if (delegateItem == null || sessionItem == null) {
        loadAttendanceTable();
        return;
    }

    int delegateId;
    int sessionId;

    try {
        delegateId = Integer.parseInt(
                delegateItem.split(" - ")[0].trim()
        );

        sessionId = Integer.parseInt(
                sessionItem.split(" - ")[0].trim()
        );

    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(
                this,
                "Could not read the delegate or session ID.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE
        );
        return;
    }

    String sql = """
            SELECT a.d_id,
                   d.f_name || ' ' || d.l_name AS delegate_name,
                   a.session_id,
                   TO_CHAR(s.session_date, 'DD-MON-YYYY') AS session_date,
                   s.s_starttime,
                   s.s_endtime,
                   a.status
            FROM attendance a
            JOIN delegate d
                ON a.d_id = d.d_id
            JOIN mun_session s
                ON a.session_id = s.session_id
            WHERE a.d_id = ?
              AND a.session_id = ?
            """;

    String[] columnNames = {
        "Delegate ID",
        "Delegate Name",
        "Session ID",
        "Session Date",
        "Start Time",
        "End Time",
        "Status"
    };

    DefaultTableModel model = new DefaultTableModel(columnNames, 0);

    try (
        Connection con = DriverManager.getConnection(
                MUNDatabaseGUI.DBURL,
                MUNDatabaseGUI.DBUSER,
                MUNDatabaseGUI.DBPASS
        );
        PreparedStatement statement = con.prepareStatement(sql)
    ) {

        statement.setInt(1, delegateId);
        statement.setInt(2, sessionId);

        try (ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                model.addRow(new Object[]{
                    resultSet.getInt("d_id"),
                    resultSet.getString("delegate_name"),
                    resultSet.getInt("session_id"),
                    resultSet.getString("session_date"),
                    resultSet.getString("s_starttime"),
                    resultSet.getString("s_endtime"),
                    resultSet.getString("status")
                });
            }
        }

        attendanceTable.setModel(model);

        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "No matching attendance record was found.",
                    "Search",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }

    } catch (SQLException e) {

        JOptionPane.showMessageDialog(
                this,
                "Could not search attendance.\n\n"
                + "Oracle error: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
    }//GEN-LAST:event_searchAttendanceButtonActionPerformed

    private void clearAttendanceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearAttendanceButtonActionPerformed
        // TODO add your handling code here:
            if (delegateAttendanceComboBox.getItemCount() > 0) {
        delegateAttendanceComboBox.setSelectedIndex(0);
    }

    if (sessionAttendanceComboBox.getItemCount() > 0) {
        sessionAttendanceComboBox.setSelectedIndex(0);
    }

    if (statusAttendanceComboBox.getItemCount() > 0) {
        statusAttendanceComboBox.setSelectedIndex(0);
    }

    attendanceTable.clearSelection();
    }//GEN-LAST:event_clearAttendanceButtonActionPerformed

    private void refreshAttendanceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshAttendanceButtonActionPerformed
        // TODO add your handling code here:
            loadAttendanceDelegates();
    loadAttendanceSessions();
    loadAttendanceStatuses();
    loadAttendanceTable();
        
    }//GEN-LAST:event_refreshAttendanceButtonActionPerformed

    private void attendanceTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_attendanceTableMouseClicked
        // TODO add your handling code here:
            int selectedRow = attendanceTable.getSelectedRow();

    if (selectedRow == -1) {
        return;
    }

    String delegateItem
            = attendanceTable.getValueAt(selectedRow, 0).toString()
            + " - "
            + attendanceTable.getValueAt(selectedRow, 1).toString();

    String sessionItem
            = attendanceTable.getValueAt(selectedRow, 2).toString()
            + " - "
            + attendanceTable.getValueAt(selectedRow, 3).toString()
            + " "
            + attendanceTable.getValueAt(selectedRow, 4).toString()
            + "-"
            + attendanceTable.getValueAt(selectedRow, 5).toString();

    delegateAttendanceComboBox.setSelectedItem(delegateItem);
    sessionAttendanceComboBox.setSelectedItem(sessionItem);

    statusAttendanceComboBox.setSelectedItem(
            attendanceTable.getValueAt(selectedRow, 6).toString()
    );
    }//GEN-LAST:event_attendanceTableMouseClicked

    private void delegatesPerCommitteeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_delegatesPerCommitteeButtonActionPerformed
        // TODO add your handling code here:
            String sql = """
            SELECT cm.com_name,
                   COUNT(d.d_id) AS number_of_delegates
            FROM committee cm
            LEFT JOIN delegate d
                ON cm.com_id = d.com_id
            GROUP BY cm.com_name
            ORDER BY cm.com_name
            """;

    String[] columnNames = {
        "Committee",
        "Number of Delegates"
    };

    String[] databaseColumns = {
        "com_name",
        "number_of_delegates"
    };

    displayReport(sql, columnNames, databaseColumns);
    }//GEN-LAST:event_delegatesPerCommitteeButtonActionPerformed

    private void awardRecipientsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_awardRecipientsButtonActionPerformed
        // TODO add your handling code here:
            String sql = """
            SELECT aw.aw_title,
                   cm.com_name,
                   d.f_name || ' ' || d.l_name AS recipient_name,
                   TO_CHAR(aw.a_date, 'DD-MON-YYYY') AS award_date
            FROM award_recipients ar
            JOIN awards aw
                ON ar.aw_id = aw.aw_id
            JOIN delegate d
                ON ar.d_id = d.d_id
            JOIN committee cm
                ON aw.com_id = cm.com_id
            ORDER BY aw.aw_title, recipient_name
            """;

    String[] columnNames = {
        "Award",
        "Committee",
        "Recipient",
        "Award Date"
    };

    String[] databaseColumns = {
        "aw_title",
        "com_name",
        "recipient_name",
        "award_date"
    };

    displayReport(sql, columnNames, databaseColumns);
    }//GEN-LAST:event_awardRecipientsButtonActionPerformed

    private void alldelegatesReportButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_alldelegatesReportButtonActionPerformed
        // TODO add your handling code here:
           String sql = """
            SELECT d.d_id,
                   d.f_name || ' ' || d.l_name AS delegate_name,
                   c.country_name,
                   cm.com_name,
                   d.regi_status
            FROM delegate d
            JOIN country c
                ON d.country_id = c.country_id
            JOIN committee cm
                ON d.com_id = cm.com_id
            ORDER BY d.d_id
            """;

    String[] columnNames = {
        "Delegate ID",
        "Delegate Name",
        "Country",
        "Committee",
        "Registration Status"
    };

    String[] databaseColumns = {
        "d_id",
        "delegate_name",
        "country_name",
        "com_name",
        "regi_status"
    };

    displayReport(sql, columnNames, databaseColumns);
    }//GEN-LAST:event_alldelegatesReportButtonActionPerformed

    private void committeeTopicsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_committeeTopicsButtonActionPerformed
        // TODO add your handling code here:
            String sql = """
            SELECT cm.com_name,
                   t.topic
            FROM committee cm
            JOIN topic t
                ON cm.com_id = t.com_id
            ORDER BY cm.com_name, t.topic
            """;

    String[] columnNames = {
        "Committee",
        "Topic"
    };

    String[] databaseColumns = {
        "com_name",
        "topic"
    };

    displayReport(sql, columnNames, databaseColumns);
    }//GEN-LAST:event_committeeTopicsButtonActionPerformed

    private void attendanceDetailsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_attendanceDetailsButtonActionPerformed
        // TODO add your handling code here:
            String sql = """
            SELECT d.f_name || ' ' || d.l_name AS delegate_name,
                   TO_CHAR(s.session_date, 'DD-MON-YYYY')
                       AS session_date,
                   s.s_starttime,
                   s.s_endtime,
                   a.status
            FROM attendance a
            JOIN delegate d
                ON a.d_id = d.d_id
            JOIN mun_session s
                ON a.session_id = s.session_id
            ORDER BY d.d_id, s.session_id
            """;

    String[] columnNames = {
        "Delegate",
        "Session Date",
        "Start Time",
        "End Time",
        "Status"
    };

    String[] databaseColumns = {
        "delegate_name",
        "session_date",
        "s_starttime",
        "s_endtime",
        "status"
    };

    displayReport(sql, columnNames, databaseColumns);
    }//GEN-LAST:event_attendanceDetailsButtonActionPerformed

    private void attendanceSummaryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_attendanceSummaryButtonActionPerformed
        // TODO add your handling code here:
            String sql = """
            SELECT d.f_name || ' ' || d.l_name AS delegate_name,
                   SUM(
                       CASE
                           WHEN a.status IN
                                ('present', 'present_and_voting')
                           THEN 1
                           ELSE 0
                       END
                   ) AS sessions_present,
                   SUM(
                       CASE
                           WHEN a.status = 'late'
                           THEN 1
                           ELSE 0
                       END
                   ) AS sessions_late,
                   SUM(
                       CASE
                           WHEN a.status = 'absent'
                           THEN 1
                           ELSE 0
                       END
                   ) AS sessions_absent
            FROM delegate d
            LEFT JOIN attendance a
                ON d.d_id = a.d_id
            GROUP BY d.f_name, d.l_name
            ORDER BY delegate_name
            """;

    String[] columnNames = {
        "Delegate",
        "Sessions Present",
        "Sessions Late",
        "Sessions Absent"
    };

    String[] databaseColumns = {
        "delegate_name",
        "sessions_present",
        "sessions_late",
        "sessions_absent"
    };

    displayReport(sql, columnNames, databaseColumns);
    }//GEN-LAST:event_attendanceSummaryButtonActionPerformed

    private void evaluationScoresButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_evaluationScoresButtonActionPerformed
        // TODO add your handling code here:
            String sql = """
            SELECT d.f_name || ' ' || d.l_name AS delegate_name,
                   ch.f_name || ' ' || ch.l_name AS chair_name,
                   cm.com_name,
                   es.score_ev
            FROM evaluation_scores es
            JOIN delegate d
                ON es.d_id = d.d_id
            JOIN chair ch
                ON es.chair_id = ch.chair_id
            JOIN committee cm
                ON es.com_id = cm.com_id
            ORDER BY es.score_ev DESC
            """;

    String[] columnNames = {
        "Delegate",
        "Chair",
        "Committee",
        "Evaluation Score"
    };

    String[] databaseColumns = {
        "delegate_name",
        "chair_name",
        "com_name",
        "score_ev"
    };

    displayReport(sql, columnNames, databaseColumns);
    }//GEN-LAST:event_evaluationScoresButtonActionPerformed

    private void positionPaperScoresButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_positionPaperScoresButtonActionPerformed
        // TODO add your handling code here:
            String sql = """
            SELECT pp.paper_no,
                   d.f_name || ' ' || d.l_name AS delegate_name,
                   cm.com_name,
                   TO_CHAR(pp.sub_date, 'DD-MON-YYYY')
                       AS submission_date,
                   pp.score
            FROM position_paper pp
            JOIN delegate d
                ON pp.d_id = d.d_id
            JOIN committee cm
                ON pp.com_id = cm.com_id
            ORDER BY pp.score DESC
            """;

    String[] columnNames = {
        "Paper Number",
        "Delegate",
        "Committee",
        "Submission Date",
        "Score"
    };

    String[] databaseColumns = {
        "paper_no",
        "delegate_name",
        "com_name",
        "submission_date",
        "score"
    };

    displayReport(sql, columnNames, databaseColumns);
    }//GEN-LAST:event_positionPaperScoresButtonActionPerformed

    private void positionPapersTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_positionPapersTableMouseClicked
        // TODO add your handling code here:
        int selectedRow = positionPapersTable.getSelectedRow();

        if (selectedRow == -1) {
            return;
        }

        paperNumberField.setText(
            positionPapersTable.getValueAt(selectedRow, 0).toString()
        );

        submissionDateField.setText(
            positionPapersTable.getValueAt(selectedRow, 1).toString()
        );

        Object contentValue =
        positionPapersTable.getValueAt(selectedRow, 2);

        contentTextArea.setText(
            contentValue == null ? "" : contentValue.toString()
        );

        scoreField.setText(
            positionPapersTable.getValueAt(selectedRow, 3).toString()
        );

        String delegateItem =
        positionPapersTable.getValueAt(selectedRow, 4).toString()
        + " - "
        + positionPapersTable.getValueAt(selectedRow, 5).toString();

        String committeeItem =
        positionPapersTable.getValueAt(selectedRow, 6).toString()
        + " - "
        + positionPapersTable.getValueAt(selectedRow, 7).toString();

        paperDelegateComboBox.setSelectedItem(delegateItem);
        paperCommitteeComboBox.setSelectedItem(committeeItem);
        paperNumberField.setEditable(false);
    }//GEN-LAST:event_positionPapersTableMouseClicked

    private void refreshPaperButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshPaperButtonActionPerformed
        // TODO add your handling code here:
        loadPaperDelegates();
        loadPaperCommittees();
        loadPositionPapersTable();
        clearPaperFields();
    }//GEN-LAST:event_refreshPaperButtonActionPerformed

    private void clearPaperButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearPaperButtonActionPerformed
        // TODO add your handling code here:
        clearPaperFields();
        positionPapersTable.clearSelection();
    }//GEN-LAST:event_clearPaperButtonActionPerformed

    private void searchPaperButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchPaperButtonActionPerformed
        // TODO add your handling code here:
        String paperNumberText = paperNumberField.getText().trim();

        if (paperNumberText.isEmpty()) {
            loadPositionPapersTable();
            return;
        }

        int paperNumber;

        try {
            paperNumber = Integer.parseInt(paperNumberText);

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(
                this,
                "Paper number must be numeric.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        String sql = """
        SELECT pp.paper_no,
        TO_CHAR(pp.sub_date, 'DD-MON-YYYY') AS sub_date,
        pp.content,
        pp.score,
        pp.d_id,
        d.f_name || ' ' || d.l_name AS delegate_name,
        pp.com_id,
        cm.com_name
        FROM position_paper pp
        JOIN delegate d
        ON pp.d_id = d.d_id
        JOIN committee cm
        ON pp.com_id = cm.com_id
        WHERE pp.paper_no = ?
        """;

        String[] columnNames = {
            "Paper Number",
            "Submission Date",
            "Content",
            "Score",
            "Delegate ID",
            "Delegate Name",
            "Committee ID",
            "Committee"
        };

        DefaultTableModel model =
        new DefaultTableModel(columnNames, 0);

        try (
            Connection con = DriverManager.getConnection(
                MUNDatabaseGUI.DBURL,
                MUNDatabaseGUI.DBUSER,
                MUNDatabaseGUI.DBPASS
            );
            PreparedStatement statement = con.prepareStatement(sql)
        ) {

            statement.setInt(1, paperNumber);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {

                    model.addRow(new Object[]{
                        resultSet.getInt("paper_no"),
                        resultSet.getString("sub_date"),
                        resultSet.getString("content"),
                        resultSet.getBigDecimal("score"),
                        resultSet.getInt("d_id"),
                        resultSet.getString("delegate_name"),
                        resultSet.getInt("com_id"),
                        resultSet.getString("com_name")
                    });
                }
            }

            positionPapersTable.setModel(model);

            if (model.getRowCount() == 0) {
                JOptionPane.showMessageDialog(
                    this,
                    "No position paper was found with number "
                    + paperNumber + ".",
                    "Search",
                    JOptionPane.INFORMATION_MESSAGE
                );
            }

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                this,
                "Could not search position papers.\n\n"
                + "Oracle error: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }//GEN-LAST:event_searchPaperButtonActionPerformed

    private void deletePaperButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deletePaperButtonActionPerformed
        // TODO add your handling code here:
        String paperNumberText = paperNumberField.getText().trim();

        if (paperNumberText.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "Select a position paper first.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int paperNumber;

        try {
            paperNumber = Integer.parseInt(paperNumberText);

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(
                this,
                "Paper number must be numeric.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int option = JOptionPane.showConfirmDialog(
            this,
            "Delete position paper " + paperNumber + "?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (option != JOptionPane.YES_OPTION) {
            return;
        }

        String sql = """
        DELETE FROM position_paper
        WHERE paper_no = ?
        """;

        try (
            Connection con = DriverManager.getConnection(
                MUNDatabaseGUI.DBURL,
                MUNDatabaseGUI.DBUSER,
                MUNDatabaseGUI.DBPASS
            );
            PreparedStatement statement = con.prepareStatement(sql)
        ) {

            statement.setInt(1, paperNumber);

            int rowsDeleted = statement.executeUpdate();

            if (rowsDeleted == 1) {

                JOptionPane.showMessageDialog(
                    this,
                    "Position paper deleted successfully.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
                );

                clearPaperFields();
                loadPositionPapersTable();

            } else {

                JOptionPane.showMessageDialog(
                    this,
                    "No position paper was found with number "
                    + paperNumber + ".",
                    "Not Found",
                    JOptionPane.WARNING_MESSAGE
                );
            }

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                this,
                "Could not delete position paper.\n\n"
                + "Oracle error: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }//GEN-LAST:event_deletePaperButtonActionPerformed

    private void updatePaperButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updatePaperButtonActionPerformed
        // TODO add your handling code here:
        String paperNumberText = paperNumberField.getText().trim();
        String submissionDate = submissionDateField.getText().trim();
        String content = contentTextArea.getText().trim();
        String scoreText = scoreField.getText().trim();

        String delegateItem =
        (String) paperDelegateComboBox.getSelectedItem();

        String committeeItem =
        (String) paperCommitteeComboBox.getSelectedItem();

        if (paperNumberText.isEmpty()
            || submissionDate.isEmpty()
            || scoreText.isEmpty()
            || delegateItem == null
            || committeeItem == null) {

            JOptionPane.showMessageDialog(
                this,
                "Paper number, submission date, score, delegate, and committee are required.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int paperNumber;
        int delegateId;
        int committeeId;
        double score;

        try {
            paperNumber = Integer.parseInt(paperNumberText);
            score = Double.parseDouble(scoreText);

            delegateId = Integer.parseInt(
                delegateItem.split(" - ")[0].trim()
            );

            committeeId = Integer.parseInt(
                committeeItem.split(" - ")[0].trim()
            );

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(
                this,
                "Paper number and IDs must be integers, and score must be numeric.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (score < 0 || score > 100) {
            JOptionPane.showMessageDialog(
                this,
                "Score must be between 0 and 100.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        if (!isValidDate(submissionDate)) {

    JOptionPane.showMessageDialog(
            this,
            "Submission date must use DD-MON-YYYY, for example 25-JAN-2026.",
            "Validation Error",
            JOptionPane.WARNING_MESSAGE
    );

    return;
}
        String sql = """
        UPDATE position_paper
        SET sub_date = TO_DATE(?, 'DD-MON-YYYY'),
        content = ?,
        score = ?,
        d_id = ?,
        com_id = ?
        WHERE paper_no = ?
        """;

        try (
            Connection con = DriverManager.getConnection(
                MUNDatabaseGUI.DBURL,
                MUNDatabaseGUI.DBUSER,
                MUNDatabaseGUI.DBPASS
            );
            PreparedStatement statement = con.prepareStatement(sql)
        ) {

            statement.setString(1, submissionDate);

            if (content.isEmpty()) {
                statement.setNull(2, java.sql.Types.VARCHAR);
            } else {
                statement.setString(2, content);
            }

            statement.setDouble(3, score);
            statement.setInt(4, delegateId);
            statement.setInt(5, committeeId);
            statement.setInt(6, paperNumber);

            int rowsUpdated = statement.executeUpdate();

            if (rowsUpdated == 1) {

                JOptionPane.showMessageDialog(
                    this,
                    "Position paper updated successfully.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
                );

                clearPaperFields();
                loadPositionPapersTable();

            } else {

                JOptionPane.showMessageDialog(
                    this,
                    "No position paper was found with number "
                    + paperNumber + ".",
                    "Not Found",
                    JOptionPane.WARNING_MESSAGE
                );
            }

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                this,
                "Could not update position paper.\n\n"
                + "The delegate can only have one position paper, "
                + "and the committee must match the delegate's committee.\n\n"
                + "Oracle error: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }//GEN-LAST:event_updatePaperButtonActionPerformed

    private void addPaperButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addPaperButtonActionPerformed
        // TODO add your handling code here:
        String paperNumberText = paperNumberField.getText().trim();
        String submissionDate = submissionDateField.getText().trim();
        String content = contentTextArea.getText().trim();
        String scoreText = scoreField.getText().trim();

        String delegateItem
        = (String) paperDelegateComboBox.getSelectedItem();

        String committeeItem
        = (String) paperCommitteeComboBox.getSelectedItem();

        if (paperNumberText.isEmpty()
            || submissionDate.isEmpty()
            || scoreText.isEmpty()
            || delegateItem == null
            || committeeItem == null) {

            JOptionPane.showMessageDialog(
                this,
                "Paper number, submission date, score, delegate, and committee are required.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        int paperNumber;
        int delegateId;
        int committeeId;
        double score;

        try {
            paperNumber = Integer.parseInt(paperNumberText);
            score = Double.parseDouble(scoreText);

            delegateId = Integer.parseInt(
                delegateItem.split(" - ")[0].trim()
            );

            committeeId = Integer.parseInt(
                committeeItem.split(" - ")[0].trim()
            );

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(
                this,
                "Paper number and IDs must be integers, and score must be numeric.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        if (score < 0 || score > 100) {

            JOptionPane.showMessageDialog(
                this,
                "Score must be between 0 and 100.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE
            );

            return;
        }
        if (!isValidDate(submissionDate)) {

    JOptionPane.showMessageDialog(
            this,
            "Submission date must use DD-MON-YYYY, for example 25-JAN-2026.",
            "Validation Error",
            JOptionPane.WARNING_MESSAGE
    );

    return;
}
        if (paperNumber < 0 || paperNumber > 9999) {
    JOptionPane.showMessageDialog(
            this,
            "Paper number must be between 0 and 9999.",
            "Validation Error",
            JOptionPane.WARNING_MESSAGE
    );
    return;
}
        String sql = """
        INSERT INTO position_paper
        (
            paper_no,
            sub_date,
            content,
            score,
            d_id,
            com_id
        )
        VALUES
        (
            ?,
            TO_DATE(?, 'DD-MON-YYYY'),
            ?,
            ?,
            ?,
            ?
        )
        """;

        try (
            Connection con = DriverManager.getConnection(
                MUNDatabaseGUI.DBURL,
                MUNDatabaseGUI.DBUSER,
                MUNDatabaseGUI.DBPASS
            );
            PreparedStatement statement = con.prepareStatement(sql)
        ) {

            statement.setInt(1, paperNumber);
            statement.setString(2, submissionDate);

            if (content.isEmpty()) {
                statement.setNull(3, java.sql.Types.VARCHAR);
            } else {
                statement.setString(3, content);
            }

            statement.setDouble(4, score);
            statement.setInt(5, delegateId);
            statement.setInt(6, committeeId);

            int rowsInserted = statement.executeUpdate();

            if (rowsInserted == 1) {

                JOptionPane.showMessageDialog(
                    this,
                    "Position paper added successfully.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
                );

                clearPaperFields();
                loadPositionPapersTable();
            }

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                this,
                "Could not add position paper.\n\n"
                + "A delegate can only have one position paper, and the selected "
                + "committee must match the delegate's committee.\n\n"
                + "Oracle error: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }//GEN-LAST:event_addPaperButtonActionPerformed

    private void committeeIdFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_committeeIdFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_committeeIdFieldActionPerformed

    public static void main(String args[]) {

        try {
            for (javax.swing.UIManager.LookAndFeelInfo info
                    : javax.swing.UIManager.getInstalledLookAndFeels()) {

                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(
                            info.getClassName()
                    );
                    break;
                }
            }

        } catch (ReflectiveOperationException
                | javax.swing.UnsupportedLookAndFeelException ex) {

            logger.log(
                    java.util.logging.Level.SEVERE,
                    null,
                    ex
            );
        }

        java.awt.EventQueue.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addAttendanceButton;
    private javax.swing.JButton addButton;
    private javax.swing.JButton addCommitteeButton;
    private javax.swing.JButton addPaperButton;
    private javax.swing.JButton alldelegatesReportButton;
    private javax.swing.JButton attendanceDetailsButton;
    private javax.swing.JPanel attendancePanel;
    private javax.swing.JButton attendanceSummaryButton;
    private javax.swing.JTable attendanceTable;
    private javax.swing.JButton awardRecipientsButton;
    private javax.swing.JTextField buildingNameField;
    private javax.swing.JButton clearAttendanceButton;
    private javax.swing.JButton clearButton;
    private javax.swing.JButton clearCommitteeButton;
    private javax.swing.JButton clearPaperButton;
    private javax.swing.JComboBox<String> committeeComboBox;
    private javax.swing.JTextField committeeIdField;
    private javax.swing.JTextField committeeNameField;
    private javax.swing.JButton committeeTopicsButton;
    private javax.swing.JTextField committeeTypeField;
    private javax.swing.JPanel committeesPanel;
    private javax.swing.JTable committeesTable;
    private javax.swing.JTextArea contentTextArea;
    private javax.swing.JComboBox<String> countryComboBox;
    private javax.swing.JComboBox<String> delegateAttendanceComboBox;
    private javax.swing.JTextField delegateIdField;
    private javax.swing.JPanel delegatesPanel;
    private javax.swing.JButton delegatesPerCommitteeButton;
    private javax.swing.JTable delegatesTable;
    private javax.swing.JButton deleteAttendanceButton;
    private javax.swing.JButton deleteButton;
    private javax.swing.JButton deleteCommitteeButton;
    private javax.swing.JButton deletePaperButton;
    private javax.swing.JTextField dobField;
    private javax.swing.JTextField emailField;
    private javax.swing.JButton evaluationScoresButton;
    private javax.swing.JTextField firstNameField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField lastNameField;
    private javax.swing.JComboBox<String> paperCommitteeComboBox;
    private javax.swing.JComboBox<String> paperDelegateComboBox;
    private javax.swing.JTextField paperNumberField;
    private javax.swing.JButton positionPaperScoresButton;
    private javax.swing.JPanel positionPapersPanel;
    private javax.swing.JTable positionPapersTable;
    private javax.swing.JButton refreshAttendanceButton;
    private javax.swing.JButton refreshButton;
    private javax.swing.JButton refreshCommitteeButton;
    private javax.swing.JButton refreshPaperButton;
    private javax.swing.JPanel reportsPanel;
    private javax.swing.JTable reportsTable;
    private javax.swing.JTextField roomNumberField;
    private javax.swing.JTextField schoolField;
    private javax.swing.JTextField scoreField;
    private javax.swing.JButton searchAttendanceButton;
    private javax.swing.JButton searchButton;
    private javax.swing.JButton searchCommitteeButton;
    private javax.swing.JButton searchPaperButton;
    private javax.swing.JComboBox<String> sessionAttendanceComboBox;
    private javax.swing.JComboBox<String> statusAttendanceComboBox;
    private javax.swing.JComboBox<String> statusComboBox;
    private javax.swing.JTextField submissionDateField;
    private javax.swing.JButton updateAttendanceButton;
    private javax.swing.JButton updateButton;
    private javax.swing.JButton updateCommitteeButton;
    private javax.swing.JButton updatePaperButton;
    // End of variables declaration//GEN-END:variables
}
