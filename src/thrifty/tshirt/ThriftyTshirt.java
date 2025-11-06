package thrifty.tshirt;

import java.util.Scanner;
import login.login;
import login.register;

public class ThriftyTshirt {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        login log = new login();
        register reg = new register();

        int choice = 0;
        boolean exit = false;

        do {
            System.out.println("\n===== Thrifty T-Shirt System =====");
            System.out.println("1. Create Account");
            System.out.println("2. Login");
            System.out.println("3. Forgot Password");
            System.out.println("4. Exit");
            System.out.print("Choose an option: ");

            String input = sc.nextLine(); 

            // Validate numeric input
            if (!input.matches("\\d+")) {
                System.out.println("❌ Invalid input! Please enter a number between 1-4.");
                continue;
            }

            choice = Integer.parseInt(input);

            switch (choice) {
                case 1:
                    reg.createAccount();
                    break;
                case 2:
                    log.loginUser();
                    break;
                case 3:
                    log.forgotPassword();
                    break;
                case 4:
                    System.out.println("👋 Exiting... Thank you!");
                    exit = true;
                    break;
                default:
                    System.out.println("❌ Invalid choice! Please choose 1-4.");
            }
        } while (!exit);
    }
}
