public final class Move {
    public enum Type {
        ADD,
        MOVE,
        REMOVE
    }

    private final Type type;
    private final int position;
    private final int row;
    private final char piece;

    public Move(Type type, int position, int row, char piece) {
        this.type = type;
        this.position = position;
        this.row = row;
        this.piece = piece;
    }

    public Type getType() {
        return type;
    }

    public int getPosition() {
        return position;
    }

    public int getRow() {
        return row;
    }

    public char getPiece() {
        return piece;
    }
}
