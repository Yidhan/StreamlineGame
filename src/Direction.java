
public enum Direction {
    
    RIGHT(0), 
    UP(1), 
    LEFT(2), 
    DOWN(3)
    ;

    private int rotationCount;

    /**
     * Constructor
     * @param rotationCount see getRotationCount()
     */
    Direction(int rotationCount) {
        this.rotationCount = rotationCount;
    }

    /**
     * A convenient method that returns rotationCount, the
     * number of clockwise rotation needed to rotate the game
     * state before moving right.
     * @return clockwise rotation count
     */
    public int getRotationCount() {
        return this.rotationCount;
    }
}