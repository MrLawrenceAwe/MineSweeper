public enum Difficulty {
    BEGINNER(9, 9, 10),
    INTERMEDIATE(16, 16, 40),
    EXPERT(30, 16, 99);

    private final int width;
    private final int height;
    private final int numMines;

    Difficulty(int width, int height, int numMines) {
        this.width = width;
        this.height = height;
        this.numMines = numMines;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getNumMines() {
        return numMines;
    }
}
