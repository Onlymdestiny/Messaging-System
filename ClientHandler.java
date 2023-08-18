import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Cilent Handler Program
 *
 * Purdue University -- CS18000 -- Spring 2022 -- Project 5
 *
 * @author William Yu, yuwl; Lamiya Laxmidhar, llaxmidh; Mohnish Harwani, mharwan; Ben Hartley, hartleyb;
 * @version July 22, 2023
 */

public class ClientHandler implements Runnable{
    public static final String SUCCESS = "success";
    public static final String FAIL = "fail";
    private Socket userSocket;
    private User currentUser;

    public ClientHandler(Socket userSocket) {
        this.userSocket = userSocket;
    }

    @Override
    public void run() {
        try {
            //read previous User data
            Optional<User> tempUser;
            BufferedReader reader = new BufferedReader(new InputStreamReader(userSocket.getInputStream()));
            PrintWriter writer = new PrintWriter(userSocket.getOutputStream());
            ArrayList<User> tempUserList = new ArrayList<>();
            ArrayList<User> tempUserList2 = new ArrayList<>();
            User tempReceiver;
            String tempString = "";
            String serverInput = "";
            String serverOutput = "";
            String[] tempSplit;

            while (true) {
                // Server login
                serverInput = reader.readLine();
                if (serverInput.equals("End system")) {
                    break;
                }
                boolean notloggedin;
                do {
                    notloggedin = true;
                    serverInput = reader.readLine();
                    if (serverInput.equals("Create account")) {
                        try {
                            serverInput = reader.readLine();
                            tempSplit = serverInput.split(",", 4);
                            Server.addUser(new User(
                                    Boolean.parseBoolean(tempSplit[0]), tempSplit[1], tempSplit[2], tempSplit[3]));
                            currentUser = Server.users.get(Server.users.size() - 1);
                            notloggedin = false;
                            writer.println(SUCCESS);
                            writer.flush();
                        } catch (UserExistException e) {
                            writer.println(e.getMessage());
                            writer.flush();
                        }
                    } else if (serverInput.equals("Log in")) {
                        serverInput = reader.readLine();
                        tempSplit = serverInput.split(",", 2);
                        tempUser = Server.authenticateUser(Server.users, tempSplit[0], tempSplit[1]);
                        if (tempUser.isPresent()) {
                            currentUser = tempUser.get();
                            writer.println(SUCCESS);
                            notloggedin = false;
                        } else {
                            writer.println(FAIL);
                        }
                        writer.flush();
                    }
                } while (notloggedin);
                boolean seller = currentUser.isUserType();

                boolean keepProgram = true;
                do {
                    // display
                    if (seller) {
                        writer.println("Seller display");
                        writer.flush();
                        serverOutput = String.join(";", currentUser.getStoreName());
                    } else {
                        writer.println("Customer display");
                        writer.flush();
                        serverOutput = Server.allVisibleStore(currentUser).stream()
                                .map(user -> String.join(",", user.getStoreName()))
                                .map(storeNames -> storeNames.isEmpty() ? "None" : storeNames)
                                .collect(Collectors.joining(";"));
                        writer.println(serverOutput);
                        writer.flush();
                        serverOutput = Server.allVisibleStore(currentUser).stream().map(
                                User::getNameOfUser).collect(Collectors.joining(";"));
                    }
                    writer.println(serverOutput);
                    writer.flush();
                    serverOutput = Server.currentVisibleConversationUser(currentUser).stream().map(User::getNameOfUser)
                            .collect(Collectors.joining(";"));
                    writer.println(serverOutput);
                    writer.flush();
                    writer.println(String.format("%s,%s", currentUser.getNameOfUser(),
                            (currentUser.isUserType()) ? "Seller" : "Buyer" ));
                    //operation
                    writer.flush();
                    int typeOfOperation;

                    serverInput = reader.readLine();
                    typeOfOperation = Integer.parseInt(serverInput);

                    switch (typeOfOperation) {
                        case 8 -> keepProgram = false;
                        case 7 -> {
                            if (!currentUser.isUserType()) {
                                writer.println("Not seller");
                                writer.flush();
                            } else {
                                writer.println("seller");
                                writer.flush();
                                serverOutput = currentUser.
                                        getStoreName().stream().collect(Collectors.joining(";"));
                                writer.println(serverOutput);
                                writer.flush();
                                serverInput = reader.readLine();

                                tempUser = Server.users.stream()
                                        .filter(n -> n.equals(currentUser)) // Filtering even numbers
                                        .findFirst();
                                if (tempUser.isPresent()) {
                                    tempUser.get().addStore(serverInput);
                                }
                                Server.userAddStore(serverInput, currentUser);
                            }
                        }
                        case 6 -> {
                            serverInput = reader.readLine();
                            switch (Integer.parseInt(serverInput)) {
                                case 0:
                                    tempString = reader.readLine();
                                    try {
                                        Server.replaceAllConversationName(tempString, currentUser);
                                    } catch (NoMessageFoundException e) {
                                        writer.println(e.getMessage());
                                        writer.flush();
                                    }
                                    Server.accountModification(tempString, Integer.parseInt(serverInput), currentUser);
                                    currentUser.setNameOfUser(tempString);
                                    break;
                                case 1:
                                    tempString = reader.readLine();
                                    Server.accountModification(tempString, Integer.parseInt(serverInput), currentUser);
                                    currentUser.setEmail(tempString);
                                    break;
                                case 2:
                                    tempString = reader.readLine();
                                    Server.accountModification(tempString, Integer.parseInt(serverInput), currentUser);
                                    currentUser.setPassword(tempString);
                                    break;
                                case 3:
                                    Server.accountModification("a", Integer.parseInt(serverInput), currentUser);
                                    Server.users.removeIf(n -> currentUser.equals(n));
                                    keepProgram = false;
                                    break;
                            }
                            writer.println(SUCCESS);
                            writer.flush();
                        }
                        case 5 -> {
                            tempUserList.clear();
                            tempUserList = currentUser.getInvisibleList();
                            serverOutput = currentUser.getInvisibleList().stream()
                                    .map(user -> user.getEmail() + "," + user.getNameOfUser())
                                    .collect(Collectors.joining(";"));
                            serverOutput = (serverOutput.isEmpty()) ? "No result" : serverOutput;
                            writer.println(serverOutput);
                            writer.flush();
                            if (!serverOutput.equals("No result")) {
                                serverInput = reader.readLine();
                                tempSplit = serverInput.split(",", 2);
                                tempUser = Server.exactPerson(tempUserList, tempSplit[0], tempSplit[1]);
                                if (tempUser.isPresent()) {
                                    Server.unInvisUser(tempUser.get(), currentUser);
                                    writer.println(SUCCESS);
                                } else {
                                    writer.println(FAIL);
                                }
                                writer.flush();
                            }
                        }
                        case 4 -> {
                            tempUserList.clear();
                            tempUserList2.clear();
                            tempUserList = Server.searchValidUser(reader.readLine(), currentUser);
                            serverOutput = tempUserList.stream()
                                    .map(user -> user.getEmail() + "," + user.getNameOfUser())
                                    .collect(Collectors.joining(";"));
                            serverOutput = (serverOutput.isEmpty()) ? "No result" : serverOutput;
                            writer.println(serverOutput);
                            writer.flush();
                            if (!serverOutput.equals("No result")) {
                                serverInput = reader.readLine();
                                tempSplit = serverInput.split(",", 2);
                                tempUser = Server.exactPerson(tempUserList, tempSplit[0], tempSplit[1]);
                                if (tempUser.isPresent()) {
                                    if (!currentUser.isInvisible(tempUser.get())) {
                                        currentUser.addInvis(tempUser.get());
                                        Server.addAction("invisible", tempUser.get(), currentUser);
                                        writer.println(SUCCESS);
                                    } else {
                                        writer.println("Already invisible this user");
                                    }
                                } else {
                                    writer.println(FAIL);
                                }
                                writer.flush();
                            }
                        }
                        case 3 -> {
                            tempUserList.clear();
                            tempUserList = currentUser.getBlockList();
                            serverOutput = currentUser.getBlockList().stream()
                                    .map(user -> user.getEmail() + "," + user.getNameOfUser())
                                    .collect(Collectors.joining(";"));
                            serverOutput = (serverOutput.isEmpty()) ? "No result" : serverOutput;
                            writer.println(serverOutput);
                            writer.flush();
                            if (!serverOutput.equals("No result")) {
                                serverInput = reader.readLine();
                                tempSplit = serverInput.split(",", 2);
                                tempUser = Server.exactPerson(tempUserList, tempSplit[0], tempSplit[1]);
                                if (tempUser.isPresent()) {
                                    Server.unBlockUser(tempUser.get(), currentUser);
                                    writer.println(SUCCESS);
                                } else {
                                    writer.println(FAIL);
                                }
                                writer.flush();
                            }
                        }
                        case 2 -> {
                            tempUserList.clear();
                            tempUserList2.clear();
                            tempUserList = Server.searchValidUser(reader.readLine(), currentUser);
                            serverOutput = tempUserList.stream()
                                    .map(user -> user.getEmail() + "," + user.getNameOfUser())
                                    .collect(Collectors.joining(";"));
                            serverOutput = (serverOutput.isEmpty()) ? "No result" : serverOutput;
                            writer.println(serverOutput);
                            writer.flush();
                            if (!serverOutput.equals("No result")) {
                                serverInput = reader.readLine();
                                tempSplit = serverInput.split(",", 2);
                                tempUser = Server.exactPerson(tempUserList, tempSplit[0], tempSplit[1]);
                                if (tempUser.isPresent()) {
                                    if (!currentUser.isBlocked(tempUser.get())) {
                                        currentUser.addBlockUser(tempUser.get());
                                        Server.addAction("block", tempUser.get(), currentUser);
                                        writer.println(SUCCESS);
                                    } else {
                                        writer.println("Already blocked this user");
                                    }
                                } else {
                                    writer.println(FAIL);
                                }
                                writer.flush();
                            }
                        }
                        case 1 -> {
                            tempUserList.clear();
                            tempUserList2.clear();
                            tempUserList = Server.searchValidUser(reader.readLine(), currentUser);
                            serverOutput = tempUserList.stream()
                                    .map(user -> user.getEmail() + "," + user.getNameOfUser())
                                    .collect(Collectors.joining(";"));
                            serverOutput = (serverOutput.isEmpty()) ? "No result" : serverOutput;
                            writer.println(serverOutput);
                            writer.flush();
                            if (!serverOutput.equals("No result")) {
                                serverInput = reader.readLine();
                                tempSplit = serverInput.split(",", 2);
                                tempUser = Server.exactPerson(tempUserList, tempSplit[0], tempSplit[1]);
                                if (tempUser.isPresent()) {
                                    if (!currentUser.isTalked(tempUser.get()) && !tempUser.get().isTalked(currentUser)) {
                                        currentUser.addConversation(tempUser.get());
                                        tempUser.get().addConversation(currentUser);
                                        Server.addAction("chat", tempUser.get(), currentUser);
                                        Server.addAction("chat", currentUser, tempUser.get());
                                        writer.println(SUCCESS);
                                    } else {
                                        writer.println("Conversation exist");
                                    }
                                } else {
                                    writer.println(FAIL);
                                }
                                writer.flush();
                            }
                        }
                        case 0 -> {
                            tempUserList.clear();
                            tempUserList = new ArrayList<>(currentUser.getConversationUser());
                            System.out.println("-----");
                            currentUser.getConversationUser().forEach(System.out::println);
                            serverOutput = currentUser.getConversationUser().stream()
                                    .map(user -> user.getEmail() + "," + user.getNameOfUser())
                                    .collect(Collectors.joining(";"));
                            System.out.println("_^-^");
                            System.out.println(serverOutput);
                            if (!tempUserList.isEmpty()) {
                                writer.println(serverOutput);
                                writer.flush();
                                serverInput = reader.readLine();
                                tempSplit = serverInput.split(",", 2);
                                tempUser = Server.exactPerson(tempUserList, tempSplit[0], tempSplit[1]);
                                if (tempUser.isPresent()) {
                                    tempReceiver = tempUser.get();
                                    writer.println(SUCCESS);
                                    writer.flush();
                                    boolean keepConversation = true;
                                    do {
                                        try {
                                            serverOutput = Server.displayMessage(
                                                    currentUser.display50Message(tempReceiver), 20);
                                            writer.println(serverOutput);
                                        } catch (NoPreviousMessageException e) {
                                            writer.println(FAIL);
                                        }
                                        writer.flush();
                                        serverInput = reader.readLine();
                                        switch (Integer.parseInt(serverInput)) {
                                            case 0 -> {
                                                serverInput = reader.readLine();
                                                if (!tempReceiver.isBlocked(currentUser)) {
                                                    if (serverInput.contains(".txt")) {
                                                        currentUser.sendTxtFile(tempReceiver, serverInput);
                                                        writer.println(SUCCESS);
                                                    } else {
                                                        currentUser.createMessage(tempReceiver, serverInput);
                                                        writer.println(SUCCESS);
                                                    }
                                                } else {
                                                    writer.println("blocked");
                                                }
                                                writer.flush();
                                            }
                                            case 1 -> {
                                                serverInput = reader.readLine();
                                                try {
                                                    serverOutput = currentUser.exactMessage(tempReceiver, serverInput);
                                                    serverOutput = (serverOutput.isEmpty()) ? "No result" : serverOutput;
                                                    writer.println(serverOutput);
                                                    writer.flush();
                                                    if (!serverOutput.equals("No result")) {
                                                        serverInput = reader.readLine();
                                                        tempSplit = serverInput.split(";", 3);
                                                        try {
                                                            currentUser.editMessage(
                                                                    tempReceiver, tempSplit[0], tempSplit[1], tempSplit[2]);
                                                        } catch (NoMessageFoundException e) {
                                                            writer.println(e.getMessage());
                                                            writer.flush();
                                                        }
                                                        writer.println(SUCCESS);
                                                        writer.flush();
                                                    }
                                                } catch(NoPreviousMessageException e){
                                                    writer.println(e.getMessage());
                                                    writer.flush();
                                                }
                                            }
                                            case 2 -> {
                                                serverInput = reader.readLine();
                                                try {
                                                    serverOutput = currentUser.exactMessage(tempReceiver, serverInput);
                                                    serverOutput = (serverOutput.isEmpty()) ? "No result" : serverOutput;
                                                    writer.println(serverOutput);
                                                    writer.flush();
                                                    if (!serverOutput.equals("No result")) {
                                                        serverInput = reader.readLine();
                                                        System.out.println(serverInput);
                                                        tempSplit = serverInput.split("-", 2);
                                                        try {
                                                            currentUser.deleteMessage(
                                                                    tempReceiver, tempSplit[1], tempSplit[0]);
                                                        } catch (NoMessageFoundException e) {
                                                            writer.println(e.getMessage());
                                                            writer.flush();
                                                        }
                                                        writer.println(SUCCESS);
                                                        writer.flush();
                                                    }
                                                } catch(NoPreviousMessageException e){
                                                    writer.println(e.getMessage());
                                                    writer.flush();
                                                }
                                            }
                                            case 3 -> keepConversation = false;
                                        }
                                    } while (keepConversation);
                                } else {
                                    writer.println(FAIL);
                                    writer.flush();
                                }
                            } else {
                                writer.println("No conversation");
                                writer.flush();
                            }
                        }
                    }
                } while (keepProgram);
            }
            userSocket.close();
        } catch (IOException e) {
            System.out.printf("%s disconnect\n", currentUser.toString());
        }
    }
}
