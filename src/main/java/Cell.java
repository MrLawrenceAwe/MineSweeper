public class Cell {
    private boolean isMine;
    private boolean isRevealed;
    private boolean isFlagged;
    private int numAdjacentMines;
    private boolean isTriggered;

    public void reveal() {
        isRevealed = true;
    }

    public void toggleFlag() {
        isFlagged = !isFlagged;
    }

    public boolean isMine() {
        return isMine;
    }

    public boolean isRevealed() {
        return isRevealed;
    }

    public void setMine(boolean mine) {
        isMine = mine;
    }

    public void setNumAdjacentMines(int numAdjacentMines) {
        this.numAdjacentMines = numAdjacentMines;
    }

    public boolean hasNoAdjacentMines() {
        return numAdjacentMines == 0;
    }

    public String toString() {
        if (isRevealed) {
            if (isMine) {
                return isTriggered ? "X" : "*";
            }
            return Integer.toString(numAdjacentMines);
        }

        return isFlagged ? "F" : ".";
    }

    public void trigger() {
        isTriggered = true;
    }
}
