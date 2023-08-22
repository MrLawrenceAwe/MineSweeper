import java.util.*;

import static java.lang.Thread.sleep;

public class Game {
    private boolean gameOver;
    private boolean playerWon;
    private Board board;
    private Difficulty difficulty;
    private int numMines;
    private Display display;
    private final Random randomNumberGenerator = new Random();
    private static final int BEGINNER_MINES = 10;
    private static final int INTERMEDIATE_MINES = 40;
    private static final int EXPERT_MINES = 99;
    ArrayList<Coordinate> mineLocations = new ArrayList<>();
    private Scanner scanner;
    private boolean playerHasNotDoneFirstReveal;

    public enum Difficulty {
        BEGINNER, INTERMEDIATE, EXPERT
    }

    public Game() {
        System.out.println("Welcome to Minesweeper!");
        establishDifficulty();
        setNumMines();
        setupNewBoard();
    }

    private void establishDifficulty() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Please select a difficulty. 'Beginner', 'Intermediate', or 'Expert'.");
            String input = scanner.nextLine().toLowerCase().trim();
            switch (input) {
                case "beginner" -> {
                    difficulty = Difficulty.BEGINNER;
                    return;
                }
                case "intermediate" -> {
                    difficulty = Difficulty.INTERMEDIATE;
                    return;
                }
                case "expert" -> {
                    difficulty = Difficulty.EXPERT;
                    return;
                }
                default -> System.out.println("Invalid difficulty. Please try again.");
            }
        }
    }

    private void setNumMines() {
        switch (difficulty) {
            case BEGINNER -> numMines = BEGINNER_MINES;
            case INTERMEDIATE -> numMines = INTERMEDIATE_MINES;
            case EXPERT -> numMines = EXPERT_MINES;
        }
    }


    private void setupNewBoard() {
        int width, height;
        switch (difficulty) {
            case BEGINNER -> {
                width = 9;
                height = 9;
            }
            case INTERMEDIATE -> {
                width = 16;
                height = 16;
            }
            case EXPERT -> {
                width = 30;
                height = 16;
            }
            default -> throw new IllegalStateException("Unexpected value: " + difficulty);
        }
        this.board = new Board(width, height);
    }

    public void start() throws InterruptedException {
        display = new Display(board);
        display.displayBoard();
        displayHelp(true);
        try {
            do { // We will break out of this loop when the game is over.
                gameLoop();
            } while (!gameOver);
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
    }

    private void gameLoop() throws InterruptedException {
        scanner = new Scanner(System.in);
        playerHasNotDoneFirstReveal = true;

        while (!gameOver) {
            display.displayMessage("Enter a command: ");
            String input = scanner.nextLine().trim();

            switch (input.toLowerCase()) {
                case "quit" -> gameOver = true;
                case "help" -> displayHelp(false);
                case "restart" -> restartGame();
                case "new game" -> startNewGame();
                default -> handleInput(input);
            }
        }
    }

    private void displayHelp(Boolean start) {
        if (start) display.displayMessage("Type 'help' for a list of commands.");
        display.displayMessage("Type 'quit' to quit the game.");
        if (!start){
            display.displayMessage("Type 'restart' to restart the game with the same difficulty.");
            display.displayMessage("Type 'new game' to start a new game with a different difficulty.");
        }
        display.displayMessage("Enter 'r x y' to reveal a cell.");
        display.displayMessage("Enter 'f x y' to flag/flag a cell.");
    }

    private void handleInput(String input) throws InterruptedException {
        String[] tokens = input.split(" ");

        if (tokens.length != 3) {
            display.displayMessage("Invalid input. Please try again.");
            return;
        }

        Coordinate coordinate = extractCoordinate(tokens[1], tokens[2]);
        if (coordinate == null || !board.isInBounds(coordinate)) {
            display.displayMessage("Invalid coordinates. Please try again.");
            return;
        }

        switch (tokens[0].toLowerCase()) {
            case "f" -> {
                board.getCell(coordinate).toggleFlag();
                display.clearScreen();
                display.displayBoard();
            }
            case "r" -> handleReveal(coordinate);
            default -> display.displayMessage("Unknown command. Type 'help' for guidance.");
        }
    }

    private void restartGame()  {
        gameOver = false;
        playerWon = false;
        playerHasNotDoneFirstReveal = true;

        setupNewBoard();
        display.setBoard(board);

        display.clearScreen();
        display.displayBoard();
        displayHelp(true);
    }

    private void startNewGame()  {
        gameOver = false;
        playerWon = false;
        playerHasNotDoneFirstReveal = true;

        display.clearScreen();
        establishDifficulty();
        setNumMines();
        setupNewBoard();
        display.setBoard(board);
        display.displayBoard();
        displayHelp(true);
    }

    private Coordinate extractCoordinate(String xStr, String yStr) {
        try {
            int x = Integer.parseInt(xStr.trim()) - 1;
            int y = Integer.parseInt(yStr.trim()) - 1;
            return new Coordinate(x, y);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void handleReveal(Coordinate coordinate) throws InterruptedException {
        if (board.getCell(coordinate).isRevealed()) {
            display.displayMessage("Cell is already revealed.");
            return;
        }

        if (playerHasNotDoneFirstReveal) {
            initialiseMineLocations(coordinate);
            placeMines(mineLocations);
            board.setAdjacentMineCounts();
            playerHasNotDoneFirstReveal = false;
        }

        boolean playerHitMine = playerHitMine(coordinate);

        revealCell(coordinate);
        if (board.getCell(coordinate).hasNoAdjacentMines() && !playerHitMine) {
            beginRevealChain(coordinate);
        }

        display.clearScreen();

        if (playerHitMine) {
            board.getCell(coordinate).trigger();
            playerWon = false;
            display.displayMessage("You triggered a mine" + coordinate.toString() + "! Game over.");
            sleep(5000);
            display.clearScreen();
            display.displayBoard();
            endGame();
        } else if (allSafeCellsRevealed()) {
            playerWon = true;
            endGame();
        } else {
            display.displayBoard();
        }
    }

    private void beginRevealChain(Coordinate coordinate)  {
        Queue<Coordinate> queue = new LinkedList<>();
        queue.add(coordinate);

        while (!queue.isEmpty()) {
            Coordinate current = queue.poll();

            List<Coordinate> adjacentCoordinates = current.getAdjacentCoordinates();
            for (Coordinate adjacentCoordinate : adjacentCoordinates) {
                if (board.isInBounds(adjacentCoordinate) && !board.getCell(adjacentCoordinate).isRevealed() && !board.getCell(adjacentCoordinate).isMine()) {
                    revealCell(adjacentCoordinate);
                    if (board.getCell(adjacentCoordinate).hasNoAdjacentMines()) {
                        queue.add(adjacentCoordinate);
                    }
                }
            }
        }
    }

    private boolean playerHitMine(Coordinate coordinate) {
        return board.getCell(coordinate).isMine();
    }

    private boolean allSafeCellsRevealed() {
        return board.allCellsRevealed();
    }

    private void initialiseMineLocations(Coordinate firstMoveCoordinate) {
        int numMineLocationsAdded = 0;
        while (numMineLocationsAdded < numMines) {
            int x = randomNumberGenerator.nextInt(board.getWidth());
            int y = randomNumberGenerator.nextInt(board.getHeight());
            Coordinate location = new Coordinate(x, y);
            if (!board.isInBounds(location)) System.out.println("Location: "+ location.toString() +"Out of bounds.");
            if (!mineLocations.contains(location) && !location.equals(firstMoveCoordinate)){
                mineLocations.add(location);
                numMineLocationsAdded++;
            }
        }
    }

    private void endGame() throws InterruptedException {
        gameOver = true;
        if (playerWon) {
            display.displayMessage("You win! Congratulations!");
            sleep(5000);
        }
        else {
            for (Coordinate mineLocation : mineLocations){
                board.getCell(mineLocation).reveal();
                display.clearScreen();
                display.displayBoard();
                sleep(100);
            }
            triggerAllMines();
            display.clearScreen();
            display.displayBoard();
            sleep(2500);
        }

        revealAllCells();
        display.clearScreen();
        display.displayBoard();
        display.displayMessage("Enter 'quit' to quit.");
        display.displayMessage("Enter 'restart' to restart the game with the same difficulty.");
        display.displayMessage("Enter 'new game' to start a new game with a different difficulty.");
        boolean validInput = false;
        while (!validInput){
            String input = scanner.nextLine().trim();
            switch (input.toLowerCase()){
                case "quit" -> {
                    gameOver = true;
                    validInput = true;
                }
                case "restart" -> {
                    restartGame();
                    validInput = true;
                }
                case "new game" -> {
                    startNewGame();
                    validInput = true;
                }
                default -> display.displayMessage("Invalid input. Please try again.");
            }
        }

    }

    private void triggerAllMines() {
        for (Coordinate mineLocation : mineLocations) board.getCell(mineLocation).trigger();
    }

    public void placeMines(ArrayList<Coordinate> locations){
        for (Coordinate location : locations) {
            placeMine(location);
        }
    }

    public void placeMine(Coordinate mineLocation) {
        board.getCell(mineLocation).setMine(true);
    }

    public void revealAllCells(){
        for (int x = 0; x < board.getWidth(); x++){
            for (int y = 0; y < board.getHeight(); y++){
                revealCell(new Coordinate(x,y));
            }
        }
    }

    public void revealCell(Coordinate coordinate) {
        board.getCell(coordinate).reveal();
    }
}
