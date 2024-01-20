package org.example;
import java.sql.*;
import java.util.Scanner;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main{

    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/library";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "Sql#huddybuddy";

    public static void main(String[] args) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            initializeDatabase(connection);
            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.println("1. Add Book");
                System.out.println("2. Delete Book");
                System.out.println("3. Update Book");
                System.out.println("4. Issue/Return Book");
                System.out.println("5. View Books Issued to a Student");
                System.out.println("6. Search Book by ID");
                System.out.println("7. Check Book Status");
                System.out.println("8. Exit");

                System.out.print("Enter your choice: ");
                int choice = scanner.nextInt();

                switch (choice) {
                    case 1:
                        addBook(connection);
                        break;
                    case 2:
                        deleteBook(connection);
                        break;
                    case 3:
                        updateBook(connection);
                        break;
                    case 4:
                        System.out.println("1. Issue Book");
                        System.out.println("2. Return Book");
                        System.out.print("Enter your choice: ");
                        int operationChoice = scanner.nextInt();

                        switch (operationChoice) {
                            case 1:
                                issueReturnBook(connection);
                                break;
                            case 2:
                                returnBook(connection);
                                break;
                            default:
                                System.out.println("Invalid operation choice. Please enter a valid option.");
                                break;
                        }
                        break;

                    case 5:
                        viewBooksIssuedToStudent(connection);
                        break;
                    case 6:
                        searchBookById(connection);
                        break;
                    case 7:
                        checkBookStatus(connection);
                        break;
                    case 8:
                        System.out.println("Exiting the system. Goodbye!");
                        System.exit(0);
                    default:
                        System.out.println("Invalid choice. Please enter a valid option.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private static void returnBook(Connection connection) throws SQLException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter book ID: ");
        int bookId = scanner.nextInt();

        // Check if the book exists
        if (!isBookExists(connection, bookId)) {
            System.out.println("No book found with the given ID.");
            return;
        }

        // Retrieve book information
        String selectBookQuery = "SELECT * FROM books WHERE id = ?";
        try (PreparedStatement selectBookStatement = connection.prepareStatement(selectBookQuery)) {
            selectBookStatement.setInt(1, bookId);

            try (ResultSet bookResultSet = selectBookStatement.executeQuery()) {
                if (bookResultSet.next()) {
                    boolean isBookIssued = bookResultSet.getBoolean("issue_status");

                    if (isBookIssued) {
                        // Book is currently issued, perform return operation
                        performReturnOperation(connection, bookResultSet);
                    } else {
                        System.out.println("The book with ID " + bookId + " is not currently issued.");
                    }
                }
            }
        }
    }

    private static void initializeDatabase(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            String createTableQuery = "CREATE TABLE IF NOT EXISTS books (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "book_name VARCHAR(255) NOT NULL," +
                    "author_name VARCHAR(255) NOT NULL," +
                    "issue_status BOOLEAN DEFAULT FALSE," +
                    "student_id INT," +
                    "due_date DATE," +
                    "fine INT DEFAULT 0)";
            statement.executeUpdate(createTableQuery);
        }
    }

    private static void addBook(Connection connection) throws SQLException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter book name: ");
        String bookName = scanner.nextLine();
        System.out.print("Enter author name: ");
        String authorName = scanner.nextLine();

        String insertQuery = "INSERT INTO books (book_name, author_name) VALUES (?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
            preparedStatement.setString(1, bookName);
            preparedStatement.setString(2, authorName);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Book added successfully!");
            } else {
                System.out.println("Failed to add the book.");
            }
        }
    }

    private static void deleteBook(Connection connection) throws SQLException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter book ID to delete: ");
        int bookId = scanner.nextInt();

        String deleteQuery = "DELETE FROM books WHERE id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(deleteQuery)) {
            preparedStatement.setInt(1, bookId);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Book deleted successfully!");
            } else {
                System.out.println("No book found with the given ID.");
            }
        }
    }

    private static void updateBook(Connection connection) throws SQLException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter book ID to update: ");
        int bookId = scanner.nextInt();

        // Assuming you want to update the author's name
        System.out.print("Enter new author name: ");
        String newAuthorName = scanner.nextLine(); // Consume the newline character
        newAuthorName = scanner.nextLine(); // Read the actual input

        String updateQuery = "UPDATE books SET author_name = ? WHERE id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
            preparedStatement.setString(1, newAuthorName);
            preparedStatement.setInt(2, bookId);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Book updated successfully!");
            } else {
                System.out.println("No book found with the given ID.");
            }
        }
    }

    private static void issueReturnBook(Connection connection) throws SQLException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter book ID: ");
        int bookId = scanner.nextInt();

        // Check if the book exists
        if (!isBookExists(connection, bookId)) {
            System.out.println("No book found with the given ID.");
            return;
        }

        // Retrieve book information
        String selectBookQuery = "SELECT * FROM books WHERE id = ?";
        try (PreparedStatement selectBookStatement = connection.prepareStatement(selectBookQuery)) {
            selectBookStatement.setInt(1, bookId);

            try (ResultSet bookResultSet = selectBookStatement.executeQuery()) {
                if (bookResultSet.next()) {
                    boolean isBookIssued = bookResultSet.getBoolean("issue_status");

                    if (isBookIssued) {
                        // Book is currently issued, perform return operation
                        performReturnOperation(connection, bookResultSet);
                    } else {
                        // Book is not issued, perform issue operation
                        performIssueOperation(connection, bookResultSet);
                    }
                }
            }
        }
    }

    private static boolean isBookExists(Connection connection, int bookId) throws SQLException {
        String checkBookQuery = "SELECT id FROM books WHERE id = ?";
        try (PreparedStatement checkBookStatement = connection.prepareStatement(checkBookQuery)) {
            checkBookStatement.setInt(1, bookId);

            try (ResultSet resultSet = checkBookStatement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private static void performIssueOperation(Connection connection, ResultSet bookResultSet) throws SQLException {
        int studentId;
        System.out.print("Enter student ID: ");
        studentId = new Scanner(System.in).nextInt();

        // Check if the student has already issued 4 books
        if (countBooksIssuedToStudent(connection, studentId) >= 4) {
            System.out.println("Cannot issue more than 4 books to a student.");
            return;
        }

        // Update book information for issuing
        String issueBookQuery = "UPDATE books SET issue_status = TRUE, student_id = ?, due_date = DATE_ADD(NOW(), INTERVAL 15 DAY) WHERE id = ?";
        try (PreparedStatement issueBookStatement = connection.prepareStatement(issueBookQuery)) {
            issueBookStatement.setInt(1, studentId);
            issueBookStatement.setInt(2, bookResultSet.getInt("id"));

            int rowsAffected = issueBookStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Book issued successfully!");
            } else {
                System.out.println("Failed to issue the book.");
            }
        }
    }

    private static void performReturnOperation(Connection connection, ResultSet bookResultSet) throws SQLException {
        // Calculate fine if the book is returned after the due date
        java.sql.Date dueDate = bookResultSet.getDate("due_date");
        java.sql.Date currentDate = java.sql.Date.valueOf(java.time.LocalDate.now());

        if (currentDate.after(dueDate)) {
            long daysLate = (currentDate.getTime() - dueDate.getTime()) / (24 * 60 * 60 * 1000);
            int fine = (int) (daysLate * 5); // Assuming Rs. 5 per day as per the requirement

            // Update book information for returning with fine
            String returnBookQuery = "UPDATE books SET issue_status = FALSE, student_id = NULL, due_date = NULL, fine = ? WHERE id = ?";
            try (PreparedStatement returnBookStatement = connection.prepareStatement(returnBookQuery)) {
                returnBookStatement.setInt(1, fine);
                returnBookStatement.setInt(2, bookResultSet.getInt("id"));

                int rowsAffected = returnBookStatement.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Book returned successfully with a fine of Rs. " + fine);
                } else {
                    System.out.println("Failed to return the book.");
                }
            }
        } else {
            // Update book information for normal return without fine
            String returnBookQuery = "UPDATE books SET issue_status = FALSE, student_id = NULL, due_date = NULL, fine = 0 WHERE id = ?";
            try (PreparedStatement returnBookStatement = connection.prepareStatement(returnBookQuery)) {
                returnBookStatement.setInt(1, bookResultSet.getInt("id"));

                int rowsAffected = returnBookStatement.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Book returned successfully!");
                } else {
                    System.out.println("Failed to return the book.");
                }
            }
        }
    }

    private static int countBooksIssuedToStudent(Connection connection, int studentId) throws SQLException {
        String countBooksQuery = "SELECT COUNT(*) FROM books WHERE student_id = ?";
        try (PreparedStatement countBooksStatement = connection.prepareStatement(countBooksQuery)) {
            countBooksStatement.setInt(1, studentId);

            try (ResultSet resultSet = countBooksStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                } else {
                    return 0;
                }
            }
        }
    }


    private static void viewBooksIssuedToStudent(Connection connection) throws SQLException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter student ID: ");
        int studentId = scanner.nextInt();

        String selectQuery = "SELECT * FROM books WHERE student_id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(selectQuery)) {
            preparedStatement.setInt(1, studentId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    System.out.println("Book ID: " + resultSet.getInt("id"));
                    System.out.println("Book Name: " + resultSet.getString("book_name"));
                    System.out.println("Author Name: " + resultSet.getString("author_name"));
                    System.out.println("Due Date: " + resultSet.getDate("due_date"));
                    // Add more fields as needed
                    System.out.println("--------------");
                }
            }
        }
    }

    private static void searchBookById(Connection connection) throws SQLException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter book ID: ");
        int bookId = scanner.nextInt();

        String selectQuery = "SELECT * FROM books WHERE id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(selectQuery)) {
            preparedStatement.setInt(1, bookId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    System.out.println("Book ID: " + resultSet.getInt("id"));
                    System.out.println("Book Name: " + resultSet.getString("book_name"));
                    System.out.println("Author Name: " + resultSet.getString("author_name"));
                    System.out.println("Issue Status: " + resultSet.getBoolean("issue_status"));
                    System.out.println("--------------");
                } else {
                    System.out.println("No book found with the given ID.");
                }
            }
        }
    }

    private static void checkBookStatus(Connection connection) throws SQLException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter student ID: ");
        int studentId = scanner.nextInt();

        String selectQuery = "SELECT * FROM books WHERE student_id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(selectQuery)) {
            preparedStatement.setInt(1, studentId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    System.out.println("Book ID: " + resultSet.getInt("id"));
                    System.out.println("Book Name: " + resultSet.getString("book_name"));
                    System.out.println("Author Name: " + resultSet.getString("author_name"));
                    System.out.println("Issue Status: " + resultSet.getBoolean("issue_status"));
                    System.out.println("Due Date: " + resultSet.getDate("due_date"));
                    System.out.println("Fine: Rs. " + resultSet.getInt("fine"));
                    System.out.println("--------------");
                }
            }
        }
    }
}
