# Chess Game - Java

A fully functional 2D Chess game built with Java Swing.

## Features

- **Complete Chess Rules**: All standard chess moves including:
  - Castling (kingside and queenside)
  - En passant captures
  - Pawn promotion (auto-promotes to Queen)
  - Check, checkmate, and stalemate detection
  
- **Visual Highlights**:
  - Selected piece highlighting (yellow)
  - Valid move squares (green)
  - Capture squares (red)
  - King in check highlighting (red)
  
- **User-Friendly Interface**:
  - Click to select a piece
  - Click again to move or deselect
  - Turn indicator
  - Status messages
  - New Game button

## How to Run

### Compile and Run
```bash
cd src
javac Piece.java GameBoard.java ChessGameSwing.java
java ChessGameSwing
```

### Or using VS Code
Just press `F5` or use the "Run" button on `ChessGameSwing.java`

## Project Structure

```
src/
├── ChessGameSwing.java  # Main GUI application (Swing-based)
├── GameBoard.java       # Game logic and move validation
├── Piece.java           # Piece class with types and symbols
├── ChessGame.java       # JavaFX version (requires JavaFX SDK)
│
│ # Legacy files (original structure):
├── Board.java           # Abstract board class
├── Index.java           # Square representation
├── Move.java            # Move record class
├── King.java            # King piece logic
├── Queen.java           # Queen piece logic
├── Rook.java            # Rook piece logic
├── Bishop.java          # Bishop piece logic
├── Knight.java          # Knight piece logic
├── Pawn.java            # Pawn piece logic
└── App.java             # Original main class
```

## Chess Notation

| Symbol | Piece |
|--------|-------|
| ♔ / ♚ | King |
| ♕ / ♛ | Queen |
| ♖ / ♜ | Rook |
| ♗ / ♝ | Bishop |
| ♘ / ♞ | Knight |
| ♙ / ♟ | Pawn |

## Controls

1. **Select a piece**: Click on one of your pieces (white moves first)
2. **Move**: Click on a highlighted square to move
3. **Deselect**: Click on the selected piece again to deselect
4. **New Game**: Click the "New Game" button to restart

## Technical Notes

### Bug Fixes from Original Code

1. **Unified Board State**: The original design had each piece class extending `Board`, causing separate board arrays. The new implementation uses a single `GameBoard` class.

2. **King Safety**: Improved check/checkmate detection with proper simulation of moves.

3. **Pawn Direction**: Fixed pawn movement to work correctly for both white (moving up) and black (moving down).

4. **Castling**: Added proper castling validation including:
   - King and rook must not have moved
   - Path must be clear
   - King cannot castle through or into check

5. **En Passant**: Properly tracks and validates en passant captures.
