import java.util.*;

public class Board {
    private final int width;
    private final int height;
    private Cell[][] cells;

    public Board(int width, int height) {
        this.width = width;
        this.height = height;
        initialiseCells(width, height);
    }

    private void initialiseCells(int width, int height) {
        cells = new Cell[width][height];
        for (int x = 0; x < width; x++){
            for (int y = 0; y < height; y++){
                cells[x][y] = new Cell();
            }
        }
    }

    public Cell getCell(Coordinate coordinate){
        return cells[coordinate.x][coordinate.y];
    }

    public int getWidth(){
        return width;
    }

    public int getHeight(){
        return height;
    }

    public void setAdjacentMineCounts(){
        for (int x = 0; x < width; x++){
            for (int y = 0; y < height; y++){
                Coordinate coordinate = new Coordinate(x, y);
                int numAdjacentMines = getNumAdjacentMines(coordinate);
                cells[x][y].setNumAdjacentMines(numAdjacentMines);
            }
        }
    }

    private int getNumAdjacentMines(Coordinate coordinate) {
        int numAdjacentMines = 0;
        List<Coordinate> adjacentCoordinates = coordinate.getAdjacentCoordinates();
        for (Coordinate adjacentCoordinate : adjacentCoordinates){
            if (isInBounds(adjacentCoordinate) && cells[adjacentCoordinate.x][adjacentCoordinate.y].isMine()){
                numAdjacentMines++;
            }
        }
        return numAdjacentMines;
    }

    boolean isInBounds(Coordinate coordinate) {
        return (coordinate.y >= 0 && coordinate.y < height) && (coordinate.x >= 0 && coordinate.x < width);
    }

    public boolean allCellsRevealed(){
        for (int row = 0; row < height; row++){
            for (int col = 0; col < width; col++){
                if (!cells[row][col].isRevealed()){
                    return false;
                }
            }
        }
        return true;
    }
}
