import java.util.*;

public class Game {
    private boolean gameOver;
    private Board board;
    private Difficulty difficulty;
    private int numMines;
    private final UserInterface userInterface = new UserInterface();
    private final Random randomNumberGenerator = new Random();
    private ArrayList<Coordinate> mineLocations;
    private boolean playerHasNotMadeFirstReveal;

    public Game() {
        userInterface.displayMessage("Welcome to Minesweeper!");
        establishDifficulty();
        this.numMines = difficulty.getNumMines();
        board = new Board(difficulty.getWidth(), difficulty.getHeight());
    }

    private static final String BEGINNER = "beginner";
    private static final String INTERMEDIATE = "intermediate";
    private static final String EXPERT = "expert";

    private void establishDifficulty() {
        while (true) {
            userInterface.displayMessage("Please select a difficulty. 'Beginner', 'Intermediate', or 'Expert'.");
            String input = userInterface.getNextLineFromUser().toLowerCase().trim();

            if (isInputValidDifficulty(input)) {
                difficulty = Difficulty.valueOf(input.toUpperCase());
                return;
            }

            userInterface.displayMessage("Invalid difficulty. Please try again.");
        }
    }

    private boolean isInputValidDifficulty(String input) {
        return BEGINNER.equals(input) || INTERMEDIATE.equals(input) || EXPERT.equals(input);
    }

    public void start() {
        userInterface.setBoard(board);
        userInterface.displayBoard();
        displayHelp(true);
        gameLoop();
        if (userInterface.getScanner() != null) {
            userInterface.closeScanner();
        }
    }

    private void gameLoop() {
        playerHasNotMadeFirstReveal = true;

        while (!gameOver) {
            userInterface.displayMessage("Enter a command: ");
            String input = userInterface.getNextLineFromUser().trim();
            handleCommand(input);
        }
    }

    private void displayHelp(boolean start) {
        if (start) userInterface.displayMessage("Type 'help' for a list of commands.");
        userInterface.displayMessage("Type 'quit' to quit the game.");
        if (!start){
            userInterface.displayMessage("Type 'restart' to restart the game with the same difficulty.");
            userInterface.displayMessage("Type 'new game' to start a new game with a different difficulty.");
        }
        userInterface.displayMessage("Enter 'r x y' to reveal a cell.");
        userInterface.displayMessage("Enter 'f x y' to flag/flag a cell.");
    }

    private void handleCommand(String input) {
        switch (input.toLowerCase()) {
            case "quit" -> gameOver = true;
            case "help" -> displayHelp(false);
            case "restart" -> resetGameWithCurrentDifficulty();
            case "new game" -> resetGameWithNewDifficulty();
            default -> handlePlayerMove(input);
        }
    }

    private void handlePlayerMove(String input) {
        String[] tokens = input.split(" ");

        if (tokens.length != 3) {
            userInterface.displayMessage("Invalid input. Please try again.");
            return;
        }

        Coordinate coordinate = decrementBothAxesAndExtractCoordinate(tokens[1], tokens[2]);
        if (coordinate == null || !board.isInBounds(coordinate)) {
            userInterface.displayMessage((coordinate != null) ? "Out of bounds ": "Invalid " +"coordinates. Please try again.");
            return;
        }

        switch (tokens[0].toLowerCase()) {
            case "f" -> {
                toggleCellFlag(coordinate);
                userInterface.clearScreenAndDisplayBoard();
            }
            case "r" -> handleReveal(coordinate);
            default -> userInterface.displayMessage("Unknown command. Type 'help' for guidance.");
        }
    }

    private void toggleCellFlag(Coordinate coordinate) {
        board.getCell(coordinate).toggleFlag();
    }

    private void reinitializeGame() {
        gameOver = false;
        playerHasNotMadeFirstReveal = true;

        board = new Board(difficulty.getWidth(), difficulty.getHeight());
        userInterface.setBoard(board);
        userInterface.clearScreenAndDisplayBoard();
        displayHelp(true);
    }

    private void resetGameWithCurrentDifficulty()  {
        reinitializeGame();
    }

    private void resetGameWithNewDifficulty()  {
        userInterface.clearScreen();
        establishDifficulty();
        this.numMines = difficulty.getNumMines();
        reinitializeGame();
    }

    Coordinate decrementBothAxesAndExtractCoordinate(String xStr, String yStr) {
        try {
            int x = Integer.parseInt(xStr.trim()) - 1;
            int y = Integer.parseInt(yStr.trim()) - 1;
            return new Coordinate(x, y);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void handleReveal(Coordinate coordinate) {
        if (isCellAlreadyRevealed(coordinate)) {
            userInterface.displayMessage("Cell is already revealed.");
            return;
        }

        if (playerHasNotMadeFirstReveal) handleFirstReveal(coordinate);

        if (cellAtLocationIsMine(coordinate)) {
            handleMineReveal(coordinate);
        } else {
            handleSafeCellReveal(coordinate);
        }
    }

    private boolean isCellAlreadyRevealed(Coordinate coordinate) {
        return board.getCell(coordinate).isRevealed();
    }

    private void handleFirstReveal(Coordinate coordinate) {
            initialiseMineLocationsExcludingPassedInLocation(coordinate);
            placeMines(mineLocations);
            board.setAdjacentMineCounts();
            playerHasNotMadeFirstReveal = false;
    }

    private void handleSafeCellReveal(Coordinate coordinate) {
        revealCellThenChainRevealIfNecessary(coordinate);

        userInterface.clearScreen();

        if (allSafeCellsRevealed()) {
            concludeGame(true);
        } else {
            userInterface.displayBoard();
        }
    }

    private void handleMineReveal(Coordinate coordinate) {
        board.getCell(coordinate).trigger();
        userInterface.displayMessage("You triggered a mine at " + coordinate.toString() + "! Game over.");
        sleep(5000);
        userInterface.clearScreenAndDisplayBoard();
        concludeGame(false);
    }

    private void revealCellThenChainRevealIfNecessary(Coordinate coordinate) {
        revealCell(coordinate);
        if (cellAtLocationHasNoAdjacentMines(coordinate)) {
            chainReveal(coordinate);
        }
    }

    private void chainReveal(Coordinate coordinate)  {
        Queue<Coordinate> queue = new LinkedList<>();
        queue.add(coordinate);

        while (!queue.isEmpty()) {
            Coordinate currentCoordinate = queue.poll();

            List<Coordinate> adjacentCoordinates = currentCoordinate.getAdjacentCoordinates();
            for (Coordinate adjacentCoordinate : adjacentCoordinates) {
                if (board.isInBounds(adjacentCoordinate) && !cellAtLocationIsRevealed(adjacentCoordinate) && !cellAtLocationIsMine(adjacentCoordinate)) {
                    revealCell(adjacentCoordinate);
                    if (cellAtLocationHasNoAdjacentMines(adjacentCoordinate)) {
                        queue.add(adjacentCoordinate);
                    }
                }
            }
        }
    }

    private boolean cellAtLocationIsRevealed(Coordinate adjacentCoordinate) {
        return board.getCell(adjacentCoordinate).isRevealed();
    }

    private boolean cellAtLocationHasNoAdjacentMines(Coordinate adjacentCoordinate) {
        return board.getCell(adjacentCoordinate).hasNoAdjacentMines();
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
            // Do nothing
        }
    }

    private boolean cellAtLocationIsMine(Coordinate coordinate) {
        return board.getCell(coordinate).isMine();
    }

    private boolean allSafeCellsRevealed() {
        return board.allSafeCellsRevealed();
    }

    private void initialiseMineLocationsExcludingPassedInLocation(Coordinate firstRevealCoordinate) {
        mineLocations = new ArrayList<>();
        int numMineLocationsAdded = 0;
        while (numMineLocationsAdded < numMines) {
            int x = randomNumberGenerator.nextInt(board.getWidth());
            int y = randomNumberGenerator.nextInt(board.getHeight());
            Coordinate location = new Coordinate(x, y);
            if (!mineLocations.contains(location) && !location.equals(firstRevealCoordinate)){
                mineLocations.add(location);
                numMineLocationsAdded++;
            }
        }
    }

    private void concludeGame(boolean playerWon) {
        gameOver = true;
        if (playerWon) {
            userInterface.displayMessage("You win! Congratulations!");
            sleep(5000);
        } else {
            doMineTriggeredSequence();
        }
        revealAllCells();
        userInterface.clearScreenAndDisplayBoard();
        displayGameConclusionHelp();
        getValidGameConclusionInputFromUser();
    }

    private void displayGameConclusionHelp() {
        userInterface.displayMessage("Enter 'quit' to quit.");
        userInterface.displayMessage("Enter 'restart' to restart the game with the same difficulty.");
        userInterface.displayMessage("Enter 'new game' to start a new game with a different difficulty.");
    }

    private void getValidGameConclusionInputFromUser() {
        boolean validInput = false;
        while (!validInput) {
            String input = userInterface.getNextLineFromUser().trim();
            switch (input.toLowerCase()) {
                case "quit" -> {
                    gameOver = true;
                    validInput = true;
                }
                case "restart" -> {
                    resetGameWithCurrentDifficulty();
                    validInput = true;
                }
                case "new game" -> {
                    resetGameWithNewDifficulty();
                    validInput = true;
                }
                default -> userInterface.displayMessage("Invalid input. Please try again.");
            }
        }
    }

    private void doMineTriggeredSequence() {
        for (Coordinate mineLocation : mineLocations) {
            board.getCell(mineLocation).reveal();
            userInterface.clearScreenAndDisplayBoard();
            switch (difficulty) {
                case BEGINNER -> sleep(500);
                case INTERMEDIATE -> sleep(200);
                case EXPERT -> sleep(50);
            }
        }
        triggerAllMines();
        userInterface.clearScreenAndDisplayBoard();
        sleep(2500);
    }

    private void triggerAllMines() {
        for (Coordinate mineLocation : mineLocations) board.getCell(mineLocation).trigger();
    }

    private void placeMines(List<Coordinate> locations){
        for (Coordinate location : locations) {
            placeMine(location);
        }
    }

    private void placeMine(Coordinate mineLocation) {
        board.getCell(mineLocation).setMine(true);
    }

    private void revealAllCells(){
        for (int x = 0; x < board.getWidth(); x++){
            for (int y = 0; y < board.getHeight(); y++){
                revealCell(new Coordinate(x,y));
            }
        }
    }

    private void revealCell(Coordinate coordinate) {
        board.getCell(coordinate).reveal();
    }

    public static int returnArg(int arg) {
        return arg;
    }
}
