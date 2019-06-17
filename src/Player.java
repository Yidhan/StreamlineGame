

import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeType;

/**
 * This class defines Player Objects
 * and provide a method to set its size.
 */
public class Player extends RoundedSquare {
    final static double STROKE_FRACTION = 0.1;

    /**
     * This is the constructor method of Player.
     * It sets a fill color,a stroke color,
     * and set the stroke type to centered
     */
    public Player() {
        this.setFill(Color.PINK);
        this.setStroke(Color.LIGHTCORAL);
        this.setStrokeType(StrokeType.CENTERED);
    }

    /**
     * Helper methods that updates the stroke width
     * and set the size for the player object
     * @param size the size of square but include the stroke
     */
    @Override
    public void setSize(double size) {
        //set the stroke width based on STROKE_FRACTION
        this.setStrokeWidth(size * STROKE_FRACTION);
        //calculate and set the size for Player Square
        super.setSize(size-size*STROKE_FRACTION);

    }
}