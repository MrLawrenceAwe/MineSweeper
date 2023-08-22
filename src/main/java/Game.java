import java.util.*;


public class Game {
    private boolean gameOver;
    private Board board;
    private Difficulty difficulty;
    private int numMines;
    private Display display;
    private final Random randomNumberGenerator = new Random();
    private ArrayList<Coordinate> mineLocations;
    private Scanner scanner;
    private boolean playerHasNotMadeFirstReveal;

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
        this.numMines = difficulty.getNumMines();
    }

    private void setupNewBoard() {
        board = new Board(difficulty.getWidth(), difficulty.getHeight());
    }

    public void start() {
        display = new Display(board);
        display.displayBoard();
        displayHelp(true);
        gameLoop();
        if (scanner != null) {
            scanner.close();
        }
    }

    private void gameLoop() {
        scanner = new Scanner(System.in);
        playerHasNotMadeFirstReveal = true;

        while (!gameOver) {
            display.displayMessage("Enter a command: ");
            String input = scanner.nextLine().trim();
            handleCommand(input);
        }
    }

    private void displayHelp(boolean start) {
        if (start) display.displayMessage("Type 'help' for a list of commands.");
        display.displayMessage("Type 'quit' to quit the game.");
        if (!start){
            display.displayMessage("Type 'restart' to restart the game with the same difficulty.");
            display.displayMessage("Type 'new game' to start a new game with a different difficulty.");
        }
        display.displayMessage("Enter 'r x y' to reveal a cell.");
        display.displayMessage("Enter 'f x y' to flag/flag a cell.");
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
            display.displayMessage("Invalid input. Please try again.");
            return;
        }

        Coordinate coordinate = decrementBothAxesAndExtractCoordinate(tokens[1], tokens[2]);
        if (coordinate == null || !board.isInBounds(coordinate)) {
            display.displayMessage((coordinate == null) ? "Out of bounds ": "Invalid " +"coordinates. Please try again.");
            return;
        }

        switch (tokens[0].toLowerCase()) {
            case "f" -> {
                toggleCellFlag(coordinate);
                display.clearScreenAndDisplayBoard();
            }
            case "r" -> handleReveal(coordinate);
            default -> display.displayMessage("Unknown command. Type 'help' for guidance.");
        }
    }

    private void toggleCellFlag(Coordinate coordinate) {
        board.getCell(coordinate).toggleFlag();
    }

    private void reinitializeGame() {
        gameOver = false;
        playerHasNotMadeFirstReveal = true;

        setupNewBoard();
        display.setBoard(board);
        display.clearScreenAndDisplayBoard();
        displayHelp(true);
    }

    private void resetGameWithCurrentDifficulty()  {
        reinitializeGame();
    }

    private void resetGameWithNewDifficulty()  {
        display.clearScreen();
        establishDifficulty();
        setNumMines();
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
            display.displayMessage("Cell is already revealed.");
            return;
        }

        if (playerHasNotMadeFirstReveal) handleFirstReveal(coordinate);

        if (isMine(coordinate)) {
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
        revealCellThenBeginRevealChainIfNecessary(coordinate);

        display.clearScreen();

        if (allSafeCellsRevealed()) {
            concludeGame(true);
        } else {
            display.displayBoard();
        }
    }

    private void handleMineReveal(Coordinate coordinate) {
        board.getCell(coordinate).trigger();
        display.displayMessage("You triggered a mine at " + coordinate.toString() + "! Game over.");
        sleep(5000);
        display.clearScreenAndDisplayBoard();
        concludeGame(false);
    }

    private void revealCellThenBeginRevealChainIfNecessary(Coordinate coordinate) {
        revealCell(coordinate);
        if (board.getCell(coordinate).hasNoAdjacentMines()) {
            beginRevealChain(coordinate);
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

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // Do nothing
        }
    }

    private boolean isMine(Coordinate coordinate) {
        return board.getCell(coordinate).isMine();
    }

    private boolean allSafeCellsRevealed() {
        return board.allCellsRevealed();
    }

    private void initialiseMineLocationsExcludingPassedInLocation(Coordinate firstMoveCoordinate) {
        mineLocations = new ArrayList<>();
        int numMineLocationsAdded = 0;
        while (numMineLocationsAdded < numMines) {
            int x = randomNumberGenerator.nextInt(board.getWidth());
            int y = randomNumberGenerator.nextInt(board.getHeight());
            Coordinate location = new Coordinate(x, y);
            if (!mineLocations.contains(location) && !location.equals(firstMoveCoordinate)){
                mineLocations.add(location);
                numMineLocationsAdded++;
            }
        }
    }

    private void concludeGame(boolean playerWon) {
        gameOver = true;
        if (playerWon) {
            display.displayMessage("You win! Congratulations!");
            sleep(5000);
        } else {
            doMineTriggeredSequence();
        }
        revealAllCells();
        display.clearScreenAndDisplayBoard();
        displayGameConclusionHelp();
        getValidEndGameInputFromUser();
    }

    private void displayGameConclusionHelp() {
        display.displayMessage("Enter 'quit' to quit.");
        display.displayMessage("Enter 'restart' to restart the game with the same difficulty.");
        display.displayMessage("Enter 'new game' to start a new game with a different difficulty.");
    }

    private void getValidEndGameInputFromUser() {
        boolean validInput = false;
        while (!validInput) {
            String input = scanner.nextLine().trim();
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
                default -> display.displayMessage("Invalid input. Please try again.");
            }
        }
    }

    private void doMineTriggeredSequence() {
        for (Coordinate mineLocation : mineLocations) {
            board.getCell(mineLocation).reveal();
            display.clearScreenAndDisplayBoard();
            switch (difficulty) {
                case BEGINNER -> sleep(500);
                case INTERMEDIATE -> sleep(250);
                case EXPERT -> sleep(100);
            }
        }
        triggerAllMines();
        display.clearScreenAndDisplayBoard();
        sleep(2500);
    }

    private void triggerAllMines() {
        for (Coordinate mineLocation : mineLocations) board.getCell(mineLocation).trigger();
    }

    public void placeMines(List<Coordinate> locations){
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
