import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Server program
 *
 * Purdue University -- CS18000 -- Spring 2022 -- Project 5
 *
 * @author William Yu, yuwl; Lamiya Laxmidhar, llaxmidh; Mohnish Harwani, mharwan; Ben Hartley, hartleyb;
 * @version July 22, 2023
 */

public class Server {
    public static final String SUCCESS = "success";
    public static final String FAIL = "fail";

    public static ArrayList<User> users;

    public synchronized static void addUser(User user) throws UserExistException {
        if (checkUniqueUser(user.getEmail(), user.getNameOfUser())) {
            users.add(user);

            ArrayList<String> tempUsers = new ArrayList<String>();
            try (BufferedReader reader = new BufferedReader(new FileReader("userInfo.csv"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    tempUsers.add(line);
                }

                tempUsers.add(String.format("%s,%s,%s,%s",(user.isUserType()) ? "true" : "false",
                        user.getEmail(), user.getPassword(), user.getNameOfUser()));
                PrintWriter pw = new PrintWriter("userinfo.csv");
                tempUsers.forEach((n) -> pw.print(n + "\n"));
                pw.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new UserExistException("User Already exist");
        }
    }

    public synchronized static void addAction(String action, User user2, User currentUser) {
        try (BufferedReader reader = new BufferedReader(new FileReader("userAction.csv"))) {
            ArrayList<String> tempAction = new ArrayList<String>();
            String line;
            while ((line = reader.readLine()) != null) {
                tempAction.add(line);
            }
            tempAction.add(String.format("%s,%s,%s", currentUser.toString(), action, user2.toString()));
            PrintWriter pw = new PrintWriter("userAction.csv");
            tempAction.forEach((n) -> pw.print(n + "\n"));
            pw.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized static void userAddStore(String storeName, User currentUser) {
        try (BufferedReader reader = new BufferedReader(new FileReader("userAction.csv"))) {
            ArrayList<String> tempAction = new ArrayList<String>();
            String line;
            while ((line = reader.readLine()) != null) {
                tempAction.add(line);
            }
            tempAction.add(String.format("%s,%s,%s", currentUser.toString(), "store", storeName));
            PrintWriter pw = new PrintWriter("userAction.csv");
            tempAction.forEach((n) -> pw.print(n + "\n"));
            pw.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized static void removeAction(String action, User user2, User currentUser) {
        try (BufferedReader reader = new BufferedReader(new FileReader("userAction.csv"))) {
            ArrayList<String> tempAction = new ArrayList<String>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.equals(String.format("%s,%s,%s", currentUser.toString(), action, user2.toString()))) {
                    tempAction.add(line);
                }
            }
            PrintWriter pw = new PrintWriter("userAction.csv");
            tempAction.forEach((n) -> pw.print(n + "\n"));
            pw.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized static void replaceAllConversationName(
            String modifier, User currentUser) throws NoMessageFoundException {
        String currentDirectory = System.getProperty("user.dir");
        File directory = new File(currentDirectory);
        File[] files = directory.listFiles((dir, name) -> name.contains(currentUser.getNameOfUser()));
        if (files != null && files.length > 0) {
            for (File file : files) {
                try {
                    String fileName = file.getName();
                    BufferedReader infoReader = new BufferedReader(new FileReader(fileName));
                    ArrayList<String> tempMessage = new ArrayList<String>();
                    String line;
                    while ((line = infoReader.readLine()) != null) {
                        tempMessage.add(line);
                    }
                    infoReader.close();
                    tempMessage.replaceAll(s -> s.contains(currentUser.getNameOfUser()) ?
                            s.replace(currentUser.getNameOfUser(), modifier) : s);
                    PrintWriter pwInfo = new PrintWriter(fileName);
                    tempMessage.forEach((n) -> pwInfo.print(n + "\n"));
                    pwInfo.close();
                    String newFileName = fileName.replace(currentUser.getNameOfUser(), modifier);
                    File newFile = new File(directory, newFileName);
                    boolean success = file.renameTo(newFile);
                } catch (IOException e) {
                    throw new NoMessageFoundException("User have no conversation");
                }
            }
        }
    }

    public synchronized static void accountModification(String modifier, int action, User currentUser) {
        try {
            BufferedReader infoReader = new BufferedReader(new FileReader("userInfo.csv"));
            BufferedReader actionReader = new BufferedReader(new FileReader("userAction.csv"));
            ArrayList<String> tempInfo = new ArrayList<String>();
            ArrayList<String> tempAction = new ArrayList<String>();
            String line;
            while ((line = infoReader.readLine()) != null) {
                tempInfo.add(line);
            }
            while ((line = actionReader.readLine()) != null) {
                tempAction.add(line);
            }
            infoReader.close();
            actionReader.close();
            switch (action) {
                case 0:
                    tempInfo.replaceAll(s -> s.contains(currentUser.getNameOfUser()) ?
                            s.replace(currentUser.getNameOfUser(), modifier) : s);
                    tempAction.replaceAll(s -> s.contains(currentUser.getNameOfUser()) ?
                            s.replace(currentUser.getNameOfUser(), modifier) : s);
                    break;
                case 1:
                    tempInfo.replaceAll(s -> s.contains(currentUser.getEmail()) ?
                            s.replace(currentUser.getEmail(), modifier) : s);
                    tempAction.replaceAll(s -> s.contains(currentUser.getEmail()) ?
                            s.replace(currentUser.getEmail(), modifier) : s);
                    break;
                case 2:
                    tempInfo.replaceAll(s -> s.contains(currentUser.getPassword()) ?
                            s.replace(currentUser.getPassword(), modifier) : s);
                    break;
                case 3:
                    tempInfo.removeIf(s -> s.contains(currentUser.getNameOfUser()) && s.contains(currentUser.getEmail()));
                    tempAction.removeIf(s -> s.contains(currentUser.getNameOfUser()) && s.contains(currentUser.getEmail()));
                    break;
            }

            PrintWriter pwInfo = new PrintWriter("userInfo.csv");
            tempInfo.forEach((n) -> pwInfo.print(n + "\n"));
            pwInfo.close();

            PrintWriter pwAction = new PrintWriter("userAction.csv");
            tempAction.forEach((n) -> pwAction.print(n + "\n"));
            pwAction.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized static boolean checkUniqueUser(String email, String nameofUser) {
        return users.stream().noneMatch(validUser -> validUser.getEmail().equals(email)
                || validUser.getNameOfUser().equals(nameofUser));
    }

    public synchronized static Optional<User> authenticateUser(ArrayList<User> listOfUser,
                                                               String email, String password) {
        return listOfUser.stream()
                .filter(user -> user.getEmail().equals(email) && user.getPassword().equals(password)).findFirst();
        // check if input email and name combination exist in the input list of user
    }

    public synchronized static void unBlockUser(User user, User currentUser) {
        ArrayList<User> tempUserList = new ArrayList<>(currentUser.getBlockList());
        tempUserList.removeIf(n -> n.equals(user));
        currentUser.setBlockList(tempUserList);
        removeAction("block", user, currentUser);
    }

    public synchronized static void unInvisUser(User user, User currentUser) {
        ArrayList<User> tempUserList = new ArrayList<>(currentUser.getInvisibleList());
        tempUserList.removeIf(n -> n.equals(user));
        currentUser.setInvisibleList(tempUserList);
        removeAction("invisible", user, currentUser);
    }

    public synchronized static Optional<User> exactPerson(ArrayList<User> listOfUser, String email, String name) {
        return listOfUser.stream()
                .filter(user -> user.getEmail().equals(email) && user.getNameOfUser().equals(name)).findFirst();
        // check if input email and name combination exist in the input list of user
    }

    public synchronized static ArrayList<User> searchValidUser(String searchingUsername, User currentUser) {
        ArrayList<User> userContainSearch = new ArrayList<>();
        users.stream().filter(n -> n.isUserType() != currentUser.isUserType())
                .forEach(userContainSearch::add); // remove same type user as currentUser
        userContainSearch.removeIf(n -> !n.getNameOfUser().toLowerCase().contains(searchingUsername.toLowerCase()));
        userContainSearch.removeIf(n -> n.isInvisible(currentUser));
        // check if any user's name contain the searching String
        return userContainSearch;
    }
//here
    public synchronized static ArrayList<User> allVisibleStore(User currentUser) {
        ArrayList<User> tempVisibleUser = new ArrayList<>(users);
        tempVisibleUser.removeIf(user -> user.isInvisible(currentUser));
        // remove sellers from list if it is invisible
        tempVisibleUser.removeIf(user -> !user.isUserType());
        // remove all customer type from the list
        return tempVisibleUser;
    }

    public synchronized static ArrayList<User> currentVisibleConversationUser(User currentUser) {
        ArrayList<User> tempVisibleUser = new ArrayList<>(currentUser.getConversationUser());
        tempVisibleUser.removeIf(user -> user.isInvisible(currentUser));
        // remove customer from list if it is invisible
        return tempVisibleUser;
    }

    public synchronized static String displayMessage(ArrayList<String[]> messages, int amount) {
        ArrayList<String> tempMessages = new ArrayList<>();
        String messagsCombined;
        int startIndex = 0;
        if (amount >= messages.size()) {
            startIndex = 0;
        } else {
            startIndex = messages.size() - amount;
        }
        messages.subList(startIndex, messages.size())
                .stream()
                .limit(amount)
                .forEach(array -> tempMessages.add(String.join("-", array)));
        messagsCombined = tempMessages.stream().collect(Collectors.joining(";"));
        return messagsCombined;
        // starting from start index, output number of amount message and join the message with -
    }

    public static void main(String[] args) throws IOException {
        final int port = 4242;
        Optional<User> tempUser;
        Optional<User> tempUser2;
        try {
            users = new ArrayList<User>();
            File file = new File("userInfo.csv");
            BufferedReader bfr = new BufferedReader(new FileReader(file));
            String line;
            while ((line = bfr.readLine()) != null) {
                String[] a = line.split(",", 4);
                users.add(new User(Boolean.parseBoolean(a[0]), a[1], a[2], a[3]));
            }
            bfr.close();
        } catch (FileNotFoundException e) {
            File file = new File("userInfo.csv");
            file.createNewFile();
        }

        try {
            int counter = 0;
            File file = new File("userAction.csv");
            BufferedReader bfr = new BufferedReader(new FileReader(file));
            String line;
            while ((line = bfr.readLine()) != null) {
                String[] a = line.split(",", 3);
                tempUser = exactPerson(users, a[0].substring(0, a[0].indexOf("-")),
                        a[0].substring(a[0].indexOf("-") + 1));
                if (tempUser.isPresent()) {
                    if (a[1].equals("store")) {
                        tempUser.get().addStore(a[2]);
                    } else {
                        tempUser2 = exactPerson(users, a[2].substring(0, a[2].indexOf("-")),
                                a[2].substring(a[2].indexOf("-") + 1));
                        if (tempUser2.isPresent()) {
                            switch (a[1]) {
                                case "block":
                                    tempUser.get().addBlockUser(tempUser2.get());
                                    break;
                                case "invisible":
                                    tempUser.get().addInvis(tempUser2.get());
                                    break;
                                case "chat":
                                    tempUser.get().addConversation(tempUser2.get());
                                    break;
                            }
                        }
                    }
                }
            }
            bfr.close();
        } catch (FileNotFoundException e) {
            File file = new File("userAction.csv");
            file.createNewFile();
        }

        // Start accepting userThread
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            boolean serverRun = true;
            while (serverRun) {
                Socket userSocket = serverSocket.accept();
                Thread userThread = new Thread(new ClientHandler(userSocket));
                userThread.start();
            }
        } catch (IOException e) {
            System.out.println("Server error");
            e.printStackTrace();
        }
    }
}