import java.io.*;
import java.text.NumberFormat;
import java.util.*;

class UserAccount {
    private String username, password;
    private double balance;

    public UserAccount(String username, String password, double balance) {
        this.username = username;
        this.password = password;
        this.balance = balance;
    }

    public String getUsername() { return username; }
    public boolean checkPassword(String inputPassword) { return password.equals(inputPassword); }
    public double getBalance() { return balance; }

    public void deposit(double amount) { if (amount > 0) balance += amount; }
    public boolean withdraw(double amount) { if (amount > 0 && balance >= amount) { balance -= amount; return true; } return false; }

    public String toFileString() { return String.format("%s,%s,%.2f", username, password, balance); }

    public static UserAccount fromFileString(String line) {
        String[] parts = line.split(",");
        return parts.length == 3 ? new UserAccount(parts[0], parts[1], Double.parseDouble(parts[2])) : null;
    }
}

public class DigitalBank {
    private static final Scanner scanner = new Scanner(System.in);
    private static final Map<String, UserAccount> users = new HashMap<>();
    private static final String USER_FILE = "users.txt";
    private static UserAccount currentUser;

    public static void main(String[] args) {
        loadUsersFromFile();
        int choice;
        do {
            showMainMenu();
            choice = scanner.nextInt(); scanner.nextLine(); // Clear buffer
            switch (choice) {
                case 1: register(); break;
                case 2: login(); break;
                case 3: exit(); break;
                default: System.out.println("Invalid choice."); break;
            }
        } while (choice != 3);
    }

    private static void showMainMenu() {
        System.out.println("\n+===========================+");
        System.out.println("|       DIGITAL BANK        |");
        System.out.println("+===========================+");
        System.out.println("| 1. Create Account         |");
        System.out.println("| 2. Login                  |");
        System.out.println("| 3. Exit                   |");
        System.out.println("+===========================+");
        System.out.print("Choose option (1-3): ");
    }

    private static void register() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        if (users.containsKey(username)) {
            System.out.println("Account already exists.");
            return;
        }

        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        users.put(username, new UserAccount(username, password, 0.0));
        saveUsersToFile();
        System.out.println("Registration successful!");
    }

    private static void login() {
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        UserAccount user = users.get(username);
        if (user != null && user.checkPassword(password)) {
            currentUser = user;
            userMenu();
        } else {
            System.out.println("Login failed.");
        }
    }

    private static void userMenu() {
        int choice;
        do {
            System.out.println("\n+=============================+");
            System.out.printf("| Welcome, %-20s |\n", currentUser.getUsername());
            System.out.println("+=============================+");
            System.out.println("| 1. View Balance              |");
            System.out.println("| 2. Deposit                   |");
            System.out.println("| 3. Withdraw                  |");
            System.out.println("| 4. View Transaction History  |");
            System.out.println("| 5. Logout                    |");
            System.out.println("+=============================+");
            System.out.print("Choose option (1-5): ");
            choice = scanner.nextInt();
            switch (choice) {
                case 1: viewBalance(); break;
                case 2: deposit(); break;
                case 3: withdraw(); break;
                case 4: viewTransactionHistory(); break;
                case 5: logout(); break;
                default: System.out.println("Invalid choice."); break;
            }
        } while (choice != 5);
    }

    private static void viewBalance() {
        System.out.println("Current Balance: " + formatCurrency(currentUser.getBalance()));
    }

    private static void deposit() {
        System.out.print("Enter deposit amount: ");
        double amount = scanner.nextDouble();
        currentUser.deposit(amount);
        saveUsersToFile();
        logTransaction("Deposit: +" + formatCurrency(amount));
        System.out.println("Deposit successful.");
    }

    private static void withdraw() {
        System.out.print("Enter withdrawal amount: ");
        double amount = scanner.nextDouble();
        if (currentUser.withdraw(amount)) {
            saveUsersToFile();
            logTransaction("Withdraw: -" + formatCurrency(amount));
            System.out.println("Withdrawal successful.");
        } else {
            System.out.println("Insufficient balance.");
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
            System.out.println("No transactions found.");
        }
    }

    private static void logout() {
        System.out.println("Logging out...");
        currentUser = null;
    }

    private static void exit() {
        System.out.print("Are you sure you want to exit? (y/n): ");
        if (scanner.nextLine().equalsIgnoreCase("y")) {
            saveUsersToFile();
            System.out.println("Goodbye!");
        }
    }

    private static void saveUsersToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USER_FILE))) {
            for (UserAccount user : users.values()) {
                writer.write(user.toFileString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving data.");
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

    private static void loadUsersFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                UserAccount user = UserAccount.fromFileString(line);
                if (user != null) users.put(user.getUsername(), user);
            }
        } catch (IOException e) {
            // No users file, nothing to load
        }
    }

    private static String formatCurrency(double amount) {
        Locale localeVN = new Locale("vi", "VN");
        return NumberFormat.getCurrencyInstance(localeVN).format(amount);
    }
}
