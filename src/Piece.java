/**
 * Represents a chess piece with its type, color, and position.
 */
public class Piece {
    
    public enum Type {
        KING('K', "♚", "♔"),
        QUEEN('Q', "♛", "♕"),
        ROOK('R', "♜", "♖"),
        BISHOP('B', "♝", "♗"),
        KNIGHT('N', "♞", "♘"),
        PAWN('P', "♟", "♙");
        
        private final char notation;
        private final String blackSymbol;
        private final String whiteSymbol;
        
        Type(char notation, String blackSymbol, String whiteSymbol) {
            this.notation = notation;
            this.blackSymbol = blackSymbol;
            this.whiteSymbol = whiteSymbol;
        }
        
        public char getNotation() { return notation; }
        public String getSymbol(String color) {
            return color.equals("white") ? whiteSymbol : blackSymbol;
        }
    }
    
    private final Type type;
    private final String color;
    private int row;
    private int col;
    private boolean hasMoved = false;
    
    public Piece(Type type, String color, int row, int col) {
        this.type = type;
        this.color = color;
        this.row = row;
        this.col = col;
    }
    
    public Type getType() { return type; }
    public String getColor() { return color; }
    public int getRow() { return row; }
    public int getCol() { return col; }
    public boolean hasMoved() { return hasMoved; }
    
    public void setPosition(int row, int col) {
        this.row = row;
        this.col = col;
        this.hasMoved = true;
    }
    
    public void setHasMoved(boolean moved) {
        this.hasMoved = moved;
    }
    
    public String getSymbol() {
        return type.getSymbol(color);
    }
    
    public char getNotation() {
        return type.getNotation();
    }
    
    /**
     * Creates a copy of this piece
     */
    public Piece copy() {
        Piece copy = new Piece(this.type, this.color, this.row, this.col);
        copy.hasMoved = this.hasMoved;
        return copy;
    }
    
    @Override
    public String toString() {
        return color + " " + type + " at (" + row + "," + col + ")";
    }
}
