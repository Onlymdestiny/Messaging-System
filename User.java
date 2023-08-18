import java.io.*;
import java.util.ArrayList;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.Scanner;

/**
 * User program
 *
 * Purdue University -- CS18000 -- Spring 2022 -- Project 4
 *
 * @author William Yu, yuwl; Lamiya Laxmidhar, llaxmidh; Mohnish Harwani, mharwan; Ben Hartley, hartleyb;
 * @version July 22, 2023
 */

public class User extends Thread {
    private boolean userType;
    private String password;
    private String email;
    private String nameOfUser;
    private ArrayList<String> storeName;
    private ArrayList<User> conversationUser;
    private ArrayList<User> blockList;
    private ArrayList<User> invisibleList;

    public User(boolean userType, String email, String password, String nameOfUser) {
        this.userType = userType;
        this.password = password;
        this.nameOfUser = nameOfUser;
        this.email = email;
        this.storeName = new ArrayList<String>();
        this.conversationUser = new ArrayList<User>();
        this.blockList = new ArrayList<User>();
        this.invisibleList = new ArrayList<User>();
    }

    public String getNameOfUser() {
        return nameOfUser;
    }

    public boolean isUserType() {
        return userType;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public void setNameOfUser(String nameOfUser) { this.nameOfUser = nameOfUser; }

    public void setUserType(boolean userType) {
        this.userType = userType;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public ArrayList<User> getConversationUser() { return conversationUser; }

    public void setConversationUser(ArrayList<User> conversationUser) { this.conversationUser = conversationUser; }

    public ArrayList<String> getStoreName() { return storeName; }

    public void setStoreName(ArrayList<String> storeName) { this.storeName = storeName; }

    public ArrayList<User> getBlockList() { return blockList; }

    public void setBlockList(ArrayList<User> blockList) { this.blockList = blockList; }

    public ArrayList<User> getInvisibleList() { return invisibleList; }

    public void setInvisibleList(ArrayList<User> invisibleList) { this.invisibleList = invisibleList; }

    public void addStore(String singleStoreName) { this.storeName.add(singleStoreName); }

    public void addBlockUser(User user) {
        this.blockList.add(user);
    }

    public void addInvis(User user) {
        this.invisibleList.add(user);
    }

    public boolean isTalked(User receiver) {
        return conversationUser.stream().anyMatch(conversationUser -> conversationUser.equals(receiver));
    }

    public boolean isBlocked(User receiver) {
        return blockList.stream().anyMatch(blockedUser -> blockedUser.equals(receiver));
    }

    public boolean isInvisible(User receiver) {
        return invisibleList.stream().anyMatch(invUser -> invUser.equals(receiver));
    }

    public boolean addConversation(User receiver) {
        boolean containsName = false;

        containsName = conversationUser.stream().anyMatch(validUser -> validUser.equals(receiver));

        if (!containsName) {
            File file = new File(String.format("%s_%s.csv", this.nameOfUser, receiver.getNameOfUser()));
            conversationUser.add(receiver);
        }
        return containsName;
    }

    public void createMessage(User receiver, String message) {
        message = commaReplaceFile(message);
        String senderAddress = String.format("%s_%s.csv", this.nameOfUser, receiver.getNameOfUser());
        String receiverAddress = String.format("%s_%s.csv", receiver.getNameOfUser(), this.nameOfUser);
        try {
            // Assemble message
            ArrayList<String> previousMessage = new ArrayList<>();
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            String tempMessage = String.format("%s,%s,%s,%s", receiver.getNameOfUser(),
                    this.nameOfUser, dtf.format(now), message);
            // sender file
            File senderFile = new File(senderAddress);
            if (senderFile.exists()) {
                Scanner scan = new Scanner(senderFile);
                while (scan.hasNextLine()) {
                    previousMessage.add(scan.nextLine());
                }
                previousMessage.add(tempMessage);

                PrintWriter pw = new PrintWriter(senderFile);
                previousMessage.forEach((n -> pw.printf(n + "\n")));
                pw.close();
            } else {
                PrintWriter fileWriter = new PrintWriter(String.format("%s_%s.csv",
                        this.nameOfUser, receiver.getNameOfUser()));
                fileWriter.println(tempMessage);
                fileWriter.close();
            }
            // receiver file
            File receiverFile = new File(receiverAddress);
            previousMessage.removeAll(previousMessage);
            if (receiverFile.exists()) {
                Scanner scan = new Scanner(receiverFile);
                while (scan.hasNextLine()) {
                    previousMessage.add(scan.nextLine());
                }
                previousMessage.add(tempMessage);

                PrintWriter pw = new PrintWriter(receiverFile);
                previousMessage.forEach((n -> pw.printf(n + "\n")));
                pw.close();
            } else {
                PrintWriter fileWriter = new PrintWriter(receiverAddress);
                fileWriter.println(tempMessage);
                fileWriter.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void editMessage(User receiver, String oldMessage, String time
            , String newMessage) throws NoMessageFoundException {
        oldMessage = commaReplaceFile(oldMessage);
        newMessage = commaReplaceFile(newMessage);
        String senderAddress = String.format("%s_%s.csv", this.nameOfUser, receiver.getNameOfUser());
        String receiverAddress = String.format("%s_%s.csv", receiver.getNameOfUser(), this.nameOfUser);
        boolean noMessageFound = true;
        try {
            // sender file
            ArrayList<String> data = new ArrayList<>();
            FileReader fr = new FileReader(senderAddress);
            BufferedReader bfr = new BufferedReader(fr);
            int counter = 0;
            String s = "";
            String temp = "";
            String[] messageDecomp;

            while (bfr.ready()) {
                temp = bfr.readLine();
                messageDecomp = temp.split(",", 4);
                if (messageDecomp[3].contains(oldMessage) && time.equals(messageDecomp[2])) {
                    messageDecomp[3] = newMessage;
                    noMessageFound = false;
                }

                s = (String.join(",", messageDecomp));
                data.add(s);
                counter++;
            }
            bfr.close();
            PrintWriter pw = new PrintWriter(senderAddress);
            data.forEach((n) -> pw.print(n + "\n")); // print all previous message in to the file
            pw.close();

            // receiver file
            data.removeAll(data);
            FileReader fr2 = new FileReader(receiverAddress);
            BufferedReader bfr2 = new BufferedReader(fr2);
            counter = 0;

            while (bfr2.ready()) {
                temp = bfr2.readLine();
                messageDecomp = temp.split(",", 4);
                if (messageDecomp[3].equals(oldMessage) && time.equals(messageDecomp[2])) {
                    messageDecomp[3] = messageDecomp[3].replace(oldMessage, newMessage);
                }

                s = (String.join(",", messageDecomp));
                data.add(s);
                counter++;
            }
            bfr2.close();

            PrintWriter pw2 = new PrintWriter(receiverAddress);
            data.forEach((n) -> pw2.print(n + "\n")); // print all previous message in to the file

            pw2.close();

            if (noMessageFound) {
                throw new NoMessageFoundException("Message not found");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteMessage(User receiver, String message, String time) throws NoMessageFoundException {
        message = commaReplaceFile(message);
        String senderAddress = String.format("%s_%s.csv", this.nameOfUser, receiver.getNameOfUser());
        File senderFile = new File(senderAddress);
        ArrayList<String[]> messages = new ArrayList<>();
        boolean noMessageFound = true;
        try (BufferedReader reader = new BufferedReader(new FileReader(senderAddress))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] messageData = line.split(",", 4);
                messages.add(messageData);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        ArrayList<String[]> updatedMessages = new ArrayList<>();

        for (String[] n : messages) {
            if (!n[3].equals(message) || !n[2].equals(time)) {
                updatedMessages.add(n);
            }
            if (n[3].equals(message) && n[2].equals(time)) {
                noMessageFound = false;
            }
        }

        try (PrintWriter writer = new PrintWriter(new File(senderAddress))) {
            for (String[] n : updatedMessages) {
                writer.print(String.join(",", n) + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        if (noMessageFound) {
            throw new NoMessageFoundException("Message not found");
        }
    }

    public ArrayList<String[]> display50Message(User receiver) throws NoPreviousMessageException {
        String senderAddress = String.format("%s_%s.csv", this.nameOfUser, receiver.getNameOfUser());
        File senderFile = new File(senderAddress);
        ArrayList<String[]> messages = new ArrayList<>();
        String tempString;
        if (senderFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(senderAddress))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.substring(line.indexOf(",") + 1);
                    String[] messageData = line.split(",", 3);
                    messageData[2] = commaReplaceDisplay(messageData[2]);
                    tempString = messageData[1];
                    messageData[1] = messageData[0];
                    messageData[0] = tempString;
                    messages.add(messageData);
                }
            } catch (IOException e) {
                throw new NoPreviousMessageException("No message");
            }

            if (messages.size() > 50) {
                int elementsToRemove = messages.size() - 50;
                messages.subList(0, elementsToRemove).clear();
            }
        } else {
            throw new NoPreviousMessageException("No message");
        }
        return messages;
    }

    public void sendTxtFile(User receiver, String fileAddress) throws FileNotFoundException {
        ArrayList<String> fileMessages = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileAddress))) {
            String line;
            while ((line = reader.readLine()) != null) {
                fileMessages.add(line);
            }
            fileMessages.forEach( n -> createMessage(receiver, n));
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("Invalid address");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String exactMessage(User receiver, String message) throws NoPreviousMessageException {
        message = commaReplaceFile(message);
        String senderAddress = String.format("%s_%s.csv", this.nameOfUser, receiver.getNameOfUser());
        File senderFile = new File(senderAddress);
        ArrayList<String> messages = new ArrayList<>();
        if (senderFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(senderAddress))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] messageData = line.split(",", 4);
                    if (messageData[3].contains(message)) {
                        messageData[3] = commaReplaceDisplay(messageData[3]);
                        messages.add(messageData[2] + "-" + messageData[3]);
                    }
                }
            } catch (IOException e) {
                throw new NoPreviousMessageException("No message");
            }
        } else {
            throw new NoPreviousMessageException("No file");
        }

        return String.join(";", messages);
    }

    public String commaReplaceFile(String message) {
        return message.replaceAll(",","---");
    }

    public String commaReplaceDisplay(String message) {
        return message.replaceAll("---",",");
    }

    public String toString() {
        return this.email + "-" + this.nameOfUser;
    }
}