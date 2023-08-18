# MessageBoard
Message Board Project for CS 180 @ Purdue

Instructions: when need to compile and run the program: first start the Server.java then start client.java for use (can start multiple client at same time)

William Yu - Submitted Vocareum work space

William Yu - Submitted Report

## Classes:

# Server.java

Handles all preivous file and record all previous action. Server will start then take in multiple clientHandler.

# Client.java 

Accepts input from the user which is sent to the server for processing and storage.

# User.java 

A class containing the information associated with a certain user. 

# ClientHandler.java

Multithread for client that allow the server to take in multiple client at same time. Serve as a middle transition class between client and server. 

## User.java

fields:

private boolean userType - the user's role

private String password - the users password.

private String email - the user's email.

private String nameOfUser - the name of the user.

private ArrayList<String> storeName - an Arraylist which stores the name of each store

private ArrayList<User> conversationUser - an Arraylist of all the users who a user has a conversation with.

private ArrayList<User> blockList - an Arraylist of all blocked users.

private ArrayList<User> invisibleList - an arraylist of all the users that the other user has been blocked by.

methods:

public User(boolean userType, String email, String password, String nameOfUser - creates a new User object with each variable set to it's appropriate arguement in the parameters.

public void addStore(String storeName) - adds a store with a given name to the list of stores.

public void addBlockUser(String storeName) - adds a user to the block list.

public void addInvis(String storeName) - adds a user to the invisible list.

public boolean isTalked(User receiver) - check if parameter user is in current user's conversation list

public boolean isBlocked(User receiver) - check if parameter user is in current user's block list

public boolean isInvisible(User receiver) - check if parameter user is in current user's isinvisiblelist

public void createMessage(User reciever, String message) - creates a new message by adding it to the previous list of messages stored in the user's CSV file.

public void editMessage(User reciever, String oldMessage, String time, String newMessage) - this edits a message by using the time and oldMessage parameters to find a match within the user's messages.  It then replaces the public void old message with the String in newMessage.

public void deleteMessage(User reciever, String time, String message) - Uses the time stamp and message text to search for a message and delete it.

public void display50messages(User reciever) - returns an arrayList of the user's past 50 messages.

public void sendTxtFile - sends a copy of a user's conversation history to another user

public String exactMessage(User receiver, String message) - find the exact message from conversation file

public String commaReplaceFile(String message) - replace comma in message with --- to avoid csv crash and future issue

public String commaReplaceDisplay(String message) - replace --- with comma for display message

public void toString - format for printing a users name

public void setInvisibleList(ArrayList<User> invisibleList) - setter for the invisible list

public void setBlockList(ArrayList<User> blockList) - setter for the block list

public void getInvisibleList(ArrayList<User> invisibleList) - getter for the invisible list

public ArrayList<User> getBlockList(ArrayList<String> blockList) - getter for the block list

public void setStoreName(ArrayList<String> storeName) - setter for the store name list

public ArrayList<String> getStoreName() - getter for the storeName arrayList

public void setConversationUser(ArrayList<User>) - setter for conversation

public ArrayList<User> getConversationUser() - getter for conversationUser

## Client.java

fields:

public static final String EXIT - text to be displayed when the user exits the program

methods:

public static void endProgramDialog() - display joptionpane for exiting the program

public static void createAccountPage() - GUI for create account page

public static ArrayList<String> createAccountCheck(ArrayList<String> info) - for create account GUI, check if user input for create account is all valid and return error message

private static void updateMenuBarAppearance(JMenuBar menuBar, Color color, int style) - update a dropdown menu in createAccount page

private static void changeMenuText(JMenu menu, String newText) - set the dropdown menu text

public static void loginPage() - GUI for log in page

public static ArrayList<String> loginCheck(ArrayList<String> info) - for log in GUI, check if user input for log in is all valid and return error message

public static void mainCustomerPage() - display customer main page

public static void mainSellerPage() - display seller main page

public static void messagePage() - display message page

## Server.java

fields:

public static ArrayList<User> users - An arraylist containing all of the users who have created an account

public static final String SUCCESS - String to be printed when the program performs and action successfully

public static final String FAIL - String to be printed when the program performs and action successfully

methods:

public static void addUser(User user) throws UserExistException - adds a new user to the arraylist of users.  If the user already exists, throws a custom exception and prints an error message.

public static void addAction(String storeName) - adds a new action to be performed when given a recipient.

public static void userAddStore(String storeName) - this method is used for a seller to add a store to the marketplace - taking in a store name as a String.

public static void removeAction - removes an action from the users Action arraylist

public static void replaceAllConversationName(String modifier) throws noMessageFoundException - Modifies a conversation name given a new name (modifier) as a parameter.

public static void accountModification(String modifier, int action) - Used to modify an account

public static boolean checkUniqueUser(String email, String nameOfUser) - checks to see if a user already exists.

public static boolean authenticateUser(User user) - verifys a user's username and password. It then logs them in if an account with the input given exists.

public static void unBlockUser(User user) - unblocks the given user.

public static void unInvisUser(User user) - makes a user visible again.

public static Optional<User> exactPerson(ArrayList<User> listOfUser, String email, String name) - Searches a list of users to see if the user exists.

public static ArrayList<User> searchValidUser(String searchingUsername) - checks to see if any user's name matches the given String

public static ArrayList<User> allVisibleStore() - searches all of the registered users and returns an arraylist of all the users that are visible.

public static ArrayList<User> currentVisibleConversationUser() - returns a list of all of the users available conversations, hiding messages that are not supposed to be visible to a given user.

public static String displayMessage() - returns a formatted String containing the users message data.
