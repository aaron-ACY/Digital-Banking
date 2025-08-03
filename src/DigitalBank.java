import java.io.*;
import java.text.NumberFormat;
import java.util.*;

class UserAccount {
    private String username;
    private String password;
    private double balance;

    public UserAccount(String username, String password, double balance) {
        this.username = username;
        this.password = password;
        this.balance = balance;
    }

    public String getUsername() {
        return username;
    }

    public boolean checkPassword(String inputPassword) {
        return password.equals(inputPassword);
    }

    public double getBalance() {
        return balance;
    }

    public void deposit(double amount) {
        if (amount > 0) balance += amount;
    }

    public boolean withdraw(double amount) {
        if (amount > 0 && balance >= amount) {
            balance -= amount;
            return true;
        }
        return false;
    }

    public String toFileString() {
        return username + "," + password + "," + balance;
    }

    public static UserAccount fromFileString(String line) {
        String[] parts = line.split(",");
        if (parts.length == 3) {
            String username = parts[0];
            String password = parts[1];
            double balance = Double.parseDouble(parts[2]);
            return new UserAccount(username, password, balance);
        }
        return null;
    }
}

public class DigitalBank {
    private static final Scanner scanner = new Scanner(System.in);
    private static final Map<String, UserAccount> users = new HashMap<>();
    private static final String USER_FILE = "users.txt";
    private static UserAccount currentUser = null;

    public static void main(String[] args) {
        loadUsersFromFile();

        int choice = 0;
        do {
            clearScreen();
            System.out.println("+===============================+");
            System.out.println("|        DIGITAL BANK           |");
            System.out.println("+===============================+");
            System.out.println("| 1. Create Account             |");
            System.out.println("| 2. Login                      |");
            System.out.println("| 3. Exit                       |");
            System.out.println("+===============================+");
            System.out.print("Choose option (1-3): ");
            choice = scanner.nextInt();
            scanner.nextLine(); // clear buffer

            switch (choice) {
                case 1 -> register();
                case 2 -> login();
                case 3 -> {
                    System.out.print("Are you sure you want to exit? (y/n): ");
                    String confirm = scanner.nextLine();
                    if (confirm.equalsIgnoreCase("y")) {
                        System.out.println("Goodbye!");
                        saveUsersToFile();
                    } else {
                        choice = 0; // return to menu
                    }
                }
                default -> System.out.println("Invalid choice.");
            }
        } while (choice != 3);
    }

    private static void register() {
        clearScreen();
        System.out.println("--- REGISTER NEW ACCOUNT ---");
        System.out.print("Enter username: ");
        String username = scanner.nextLine();

        if (users.containsKey(username)) {
            System.out.println("Account already exists.");
            return;
        }

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        UserAccount newUser = new UserAccount(username, password, 0.0);
        users.put(username, newUser);
        saveUsersToFile();
        System.out.println("Registration successful!");
    }

    private static void login() {
        clearScreen();
        System.out.println("--- LOGIN ---");
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        UserAccount user = users.get(username);
        if (user != null && user.checkPassword(password)) {
            currentUser = user;
            System.out.println("Login successful!");
            userMenu();
        } else {
            System.out.println("Login failed.");
        }
    }

    private static void userMenu() {
        int choice;
        do {
            clearScreen();
            System.out.println("+=====================================+");
            System.out.printf ("| Welcome, %-25s |\n", currentUser.getUsername());
            System.out.println("+=====================================+");
            System.out.println("| 1. View Balance                    |");
            System.out.println("| 2. Deposit                         |");
            System.out.println("| 3. Withdraw                        |");
            System.out.println("| 4. View Transaction History        |");
            System.out.println("| 5. Logout                          |");
            System.out.println("+=====================================+");
            System.out.print("Choose option (1-5): ");
            choice = scanner.nextInt();

            switch (choice) {
                case 1 -> {
                    System.out.println("Current Balance: " + formatCurrency(currentUser.getBalance()));
                }
                case 2 -> {
                    System.out.print("Enter amount to deposit: ");
                    double amount = scanner.nextDouble();
                    currentUser.deposit(amount);
                    saveUsersToFile();
                    logTransaction("Deposit: +" + formatCurrency(amount));
                    System.out.println("Deposit successful.");
                }
                case 3 -> {
                    System.out.print("Enter amount to withdraw: ");
                    double amount = scanner.nextDouble();
                    if (currentUser.withdraw(amount)) {
                        saveUsersToFile();
                        logTransaction("Withdraw: -" + formatCurrency(amount));
                        System.out.println("Withdrawal successful.");
                    } else {
                        System.out.println("Insufficient balance.");
                    }
                }
                case 4 -> viewTransactionHistory();
                case 5 -> {
                    System.out.println("Logging out...");
                    currentUser = null;
                }
                default -> System.out.println("Invalid choice.");
            }

            if (choice != 5) {
                System.out.print("Press Enter to continue...");
                scanner.nextLine(); // Wait for user input
                scanner.nextLine(); // Read the blank line after number
            }

        } while (choice != 5);
    }

    private static void loadUsersFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                UserAccount user = UserAccount.fromFileString(line);
                if (user != null) {
                    users.put(user.getUsername(), user);
                }
            }
        } catch (IOException e) {
            // If file does not exist, no need to handle
        }
    }

    private static void saveUsersToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USER_FILE))) {
            for (UserAccount user : users.values()) {
                writer.write(user.toFileString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving account data.");
        }
    }

    private static void logTransaction(String message) {
        String filename = "logs_" + currentUser.getUsername() + ".txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true))) {
            writer.write(new Date() + " - " + message);
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Unable to log transaction.");
        }
    }

    private static void viewTransactionHistory() {
        String filename = "logs_" + currentUser.getUsername() + ".txt";
        System.out.println("\n--- TRANSACTION HISTORY ---");
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.out.println("No transactions available.");
        }
    }

    private static String formatCurrency(double amount) {
        Locale localeVN = new Locale("vi", "VN");
        NumberFormat currencyVN = NumberFormat.getInstance(localeVN);
        return currencyVN.format(amount) + " VND";
    }

    private static void clearScreen() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            // Do nothing if clearing screen fails
        }
    }
}
