# Connect4

Our take on the classic game of Connect 4. The client and server programs are built using Java socket programming for handling backend tasks, 
while the game GUI was built using Java Swing.

How to play:

    Game Rules:
    
    For those unfamiliar with the rules of Connect 4, here is a quick rundown:
    
        - The goal of each player is to get 4 of their pieces "connected" in a continuos row before their opponent.
        
        - Players take turns placing pieces on the board (pieces are placed from bottom to top and will stack on top eachother).
        
        - The game can be won by a player getting 4 pieces in a row either vertically, horizontally, or diagonally.
        
        - If the board is completely filled before either player wins, the game results in a tie.

    
    Getting started:
    
        1. First off, you will need to download the game source files.
        
        2. 
    
    The game is started by two clients connecting to the server. The server supports multithreading so multiple games can be played at the same time.
    
        - Clients are paired based on the order that they connect to the server. (i.e. the first client will be paired with the second client, the third
        will paired with the fourth etc...)
        
     
