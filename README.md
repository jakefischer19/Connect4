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
    
        1. First off, you will need to download the game source files and add them to a java project.
        
        
        2. Once downloaded, you will need to create a run configuration for your java project in your IDE. I will use NetBeans as an example:
        
            - Open NetBeans and locate your Connect 4 project folder.
            
            
            - Right click the project folder and go to - Set Configuration > Customize
            
            
            - This will open your project properties menu. Once here, click on "Run" in the Categories menu on the left.
            
            
            - Next, find the "Browse" button next to the "Main Class" field and select the GameClient file as your main class.
            
            
            - Then in the "Arguments" field, you will need to enter the IP address of your server.
            
                - if you are running your server locally on your computer, enter "localhost"
            
            
            - Click the "OK" button and you are now ready to begin.
        
        
        3. Next start the server and ensure that it is running.
        
        
        4. Finally, each client will need to run their Connect 4 project to connect to the server.
        
    
    The game is started by two clients connecting to the server. The server supports multithreading so multiple games can be played at the same time.
    
        - Clients are paired based on the order that they connect to the server. (i.e. the first client will be paired with the second client, the third
        will paired with the fourth etc...)
        
     
     Enjoy!!
     
