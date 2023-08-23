import java.util.Scanner;

public class UserInterface {
    private Board board;
    private final Scanner scanner;

    public UserInterface(){
        scanner = new Scanner(System.in);
    }

    public void displayBoard() {
        if (board == null) return;
        int maxWidth = Integer.toString(Math.max(board.getWidth(), board.getHeight())).length();

        printSpaces(maxWidth + 1);
        for (int j = 0; j < board.getWidth(); j++) {
            System.out.printf("%" + maxWidth + "d ", j + 1);
        }
        System.out.println();

        for (int y = 0; y < board.getHeight(); y++) {
            System.out.printf("%" + maxWidth + "d ", y + 1);
            for (int x = 0; x < board.getWidth(); x++) {
                Coordinate coordinate = new Coordinate(x, y);
                System.out.printf("%" + maxWidth + "s ", board.getCell(coordinate).toString());
            }
            System.out.println();
        }
    }

    private void printSpaces(int numSpaces) {
        for (int i = 0; i < numSpaces; i++) {
            System.out.print(' ');
        }
    }

    public void displayMessage(String message) {
        System.out.println(message);
    }

    public void clearScreen() {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            // Windows-specific clear screen command
            try {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } catch (Exception e) {
                // If there's an error (like in IntelliJ), print newlines
                for(int i = 0; i < 100; i++) {
                    System.out.println();
                }
            }
        } else {
            // Assuming Unix-based terminals (like macOS or Linux)
            System.out.print("\033[H\033[2J");
            System.out.flush();

            // If the above escape code doesn't work (like in IntelliJ), print newlines
            if (System.console() == null) {
                for(int i = 0; i < 100; i++) {
                    System.out.println();
                }
            }
        }
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public void clearScreenAndDisplayBoard() {
        clearScreen();
        displayBoard();
    }

    public String getNextLineFromUser() {
        return scanner.nextLine();
    }

    public Scanner getScanner() {
        return scanner;
    }

    public void closeScanner() {
        scanner.close();
    }
}
