import java.io.*;
import java.text.NumberFormat;
import java.util.*;

public class DigitalBank {
    private static final Scanner scanner = new Scanner(System.in);
    private static double balance = 0.0;

    public static void main(String[] args) {
        int choice = 0;
        do {
            clearScreen();
            System.out.println("+===============================+");
            System.out.println("|        DIGITAL BANK           |");
            System.out.println("+===============================+");
            System.out.println("| 1. View Balance               |");
            System.out.println("| 2. Deposit                    |");
            System.out.println("| 3. Withdraw                   |");
            System.out.println("| 4. View Transaction History   |");
            System.out.println("| 5. Exit                       |");
            System.out.println("+===============================+");
            System.out.print("Choose option (1-5): ");
            choice = scanner.nextInt();
            scanner.nextLine(); // clear buffer

            switch (choice) {
                case 1 -> viewBalance();
                case 2 -> deposit();
                case 3 -> withdraw();
                case 4 -> viewTransactionHistory();
                case 5 -> System.out.println("Goodbye!");
                default -> System.out.println("Invalid choice.");
            }

            if (choice != 5) {
                System.out.print("Press Enter to continue...");
                scanner.nextLine(); // Wait for user input
            }

        } while (choice != 5);
    }

    private static void viewBalance() {
        System.out.println("Current Balance: " + formatCurrency(balance));
    }

    private static void deposit() {
        System.out.print("Enter amount to deposit: ");
        double amount = scanner.nextDouble();
        if (amount > 0) {
            balance += amount;
            logTransaction("Deposit: +" + formatCurrency(amount));
            System.out.println("Deposit successful.");
        } else {
            System.out.println("Invalid deposit amount.");
        }
    }

    private static void withdraw() {
        System.out.print("Enter amount to withdraw: ");
        double amount = scanner.nextDouble();
        if (amount > 0 && balance >= amount) {
            balance -= amount;
            logTransaction("Withdraw: -" + formatCurrency(amount));
            System.out.println("Withdrawal successful.");
        } else {
            System.out.println("Insufficient balance or invalid amount.");
        }
    }

    private static void viewTransactionHistory() {
        String filename = "logs.txt";
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

    private static void logTransaction(String message) {
        String filename = "logs.txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true))) {
            writer.write(new Date() + " - " + message);
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Unable to log transaction.");
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
