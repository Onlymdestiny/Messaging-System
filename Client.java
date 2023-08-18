import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Cilent program
 *
 * Purdue University -- CS18000 -- Spring 2022 -- Project 5
 *
 * @author William Yu, yuwl; Lamiya Laxmidhar, llaxmidh; Mohnish Harwani, mharwan; Ben Hartley, hartleyb;
 * @version July 22, 2023
 */

public class Client {
    public static final String EMAIL_PATTERN = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    public static final Pattern EMAIL_REGEX_PATTERN = Pattern.compile(EMAIL_PATTERN);
    public static final String NAME_PATTERN = "[A-Z][a-zA-Z]*";
    public static final String GUI_TITLE = "Messager";
    public static final Object LOCK = new Object();
    public static String GUI_PASS = new String();
    public static int OPTION_PASS;
    public static ArrayList<String> storeName;
    public static ArrayList<String> sellerName;
    public static ArrayList<String> conversationUserList;
    public static String currentUser;
    public static boolean isFrameDisposed;
    public static ArrayList<String> messageList = new ArrayList<>();

    public static void endProgramDialog() {
        JOptionPane.showMessageDialog(null, "Thank you for using Messenger!",
                "Exiting", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void createAccountPage() {
        final int[] roleOutput = {0};
        ArrayList<String> info = new ArrayList<String>();
        int line = 0;
        JFrame frame = new JFrame("Create Account");
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gridBagConstraints = new GridBagConstraints();

        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints.gridy = line++;
        JLabel email = new JLabel("Email: ");
        panel.add(email, gridBagConstraints);
        gridBagConstraints.anchor = GridBagConstraints.LINE_END;
        JTextField emailText = new JTextField("", 20);
        panel.add(emailText, gridBagConstraints);

        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints.gridy = line++;
        JLabel password = new JLabel("Password: ");
        panel.add(password, gridBagConstraints);
        gridBagConstraints.anchor = GridBagConstraints.LINE_END;
        JTextField passwordText = new JTextField(20);
        panel.add(passwordText, gridBagConstraints);

        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints.gridy = line++;
        JLabel role = new JLabel("Role Menu:");
        panel.add(role, gridBagConstraints);
        JMenuBar roleBar = new JMenuBar();
        JMenu roleMenu = new JMenu("Buyer");
        roleMenu.setFont(new Font("Arial", Font.BOLD, 13));
        roleMenu.setForeground(Color.RED);
        JMenuItem buyerItem = new JMenuItem("Buyer");
        JMenuItem sellerItem = new JMenuItem("Seller");
        roleMenu.add(buyerItem);
        roleMenu.add(sellerItem);
        roleBar.add(roleMenu);
        panel.add(roleBar, gridBagConstraints);

        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints.gridy = line++;
        JLabel name = new JLabel("Name: ");
        panel.add(name, gridBagConstraints);
        gridBagConstraints.anchor = GridBagConstraints.LINE_END;
        JTextField nameText = new JTextField(20);
        panel.add(nameText, gridBagConstraints);

        JButton confirm = new JButton("Confirm");
        gridBagConstraints.gridy = line;
        panel.add(confirm, gridBagConstraints);

        buyerItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                roleOutput[0] = 0;
                changeMenuText(roleMenu, "Buyer");
                updateMenuBarAppearance(roleBar, Color.RED, Font.BOLD);
            }
        });

        sellerItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                roleOutput[0] = 1;
                changeMenuText(roleMenu, "Seller");
                updateMenuBarAppearance(roleBar, Color.BLUE, Font.BOLD);
            }
        });

        confirm.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                synchronized (LOCK) {
                    info.clear();
                    info.add(emailText.getText());
                    info.add(passwordText.getText());
                    info.add(nameText.getText());
                    if (info.stream().noneMatch(String::isEmpty)) {
                        if (createAccountCheck(info).isEmpty()) {
                            GUI_PASS = (roleOutput[0] == 1) ? "true" : "false";
                            GUI_PASS += "," + info.get(0) + "," + info.get(1) + "," + info.get(2);
                            LOCK.notify();
                            frame.dispose();
                        } else {
                            JOptionPane.showMessageDialog(null, String.join("\n",
                                    createAccountCheck(info)), "Errors Found", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Please fill all information",
                                "Errors Found", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                synchronized (LOCK) {
                    int result = JOptionPane.showConfirmDialog(frame, "Are you sure you want to close the window?",
                            "Confirm Close", JOptionPane.YES_NO_OPTION);
                    if (result == JOptionPane.YES_OPTION) {
                        isFrameDisposed = true;
                        LOCK.notify();
                        frame.dispose(); // Close the frame
                    }
                }
            }
        });

        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setVisible(true);
    }

    public static ArrayList<String> createAccountCheck(ArrayList<String> info) {
        int line = 0;
        ArrayList<String> tempErrorList = new ArrayList<>();
        if (!EMAIL_REGEX_PATTERN.matcher(info.get(line++)).matches()) {
            tempErrorList.add("Invalid email format");
        }
        String tempString = info.get(++line);
        if (Arrays.stream(tempString.split("\\s+")).
                noneMatch(word -> word.matches(NAME_PATTERN)) || !tempString.contains(" ")) {
            tempErrorList.add("Invalid name format." +
                    " Name need to have every part's first letter uppercase");
        }
        return tempErrorList;
    }

    private static void updateMenuBarAppearance(JMenuBar menuBar, Color color, int style) {
        for (int i = 0; i < menuBar.getMenuCount(); i++) {
            JMenu menu = menuBar.getMenu(i);
            menu.setFont(new Font(menu.getFont().getName(), style, menu.getFont().getSize()));
            menu.setForeground(color);
        }
    }

    private static void changeMenuText(JMenu menu, String newText) {
        menu.setText(newText);
    }

    public static void loginPage() {
        ArrayList<String> info = new ArrayList<String>();
        int line = 0;
        JFrame frame = new JFrame("Log in");
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gridBagConstraints = new GridBagConstraints();

        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints.gridy = line++;
        JLabel email = new JLabel("Email: ");
        panel.add(email, gridBagConstraints);
        gridBagConstraints.anchor = GridBagConstraints.LINE_END;
        JTextField emailText = new JTextField("", 20);
        panel.add(emailText, gridBagConstraints);

        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints.gridy = line++;
        JLabel password = new JLabel("Password: ");
        panel.add(password, gridBagConstraints);
        gridBagConstraints.anchor = GridBagConstraints.LINE_END;
        JPasswordField passwordText = new JPasswordField(20);
        panel.add(passwordText, gridBagConstraints);

        JButton confirm = new JButton("Confirm");
        gridBagConstraints.gridy = line;
        panel.add(confirm, gridBagConstraints);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                synchronized (LOCK) {
                    int result = JOptionPane.showConfirmDialog(frame, "Are you sure you want to close the window?",
                            "Confirm Close", JOptionPane.YES_NO_OPTION);
                    if (result == JOptionPane.YES_OPTION) {
                        isFrameDisposed = true;
                        LOCK.notify();
                        frame.dispose(); // Close the frame
                    }
                }
            }
        });

        confirm.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                synchronized (LOCK) {
                    info.clear();
                    info.add(emailText.getText());
                    info.add(passwordText.getText());
                    if (info.stream().noneMatch(String::isEmpty)) {
                        if (loginCheck(info).isEmpty()) {
                            GUI_PASS = new String(String.join(",", info));
                            LOCK.notify();
                            frame.dispose();
                        } else {
                            JOptionPane.showMessageDialog(null, String.join("\n",
                                    loginCheck(info)), "Errors Found", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Please fill all information",
                                "Errors Found", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setVisible(true);
    }

    public static ArrayList<String> loginCheck(ArrayList<String> info) {
        ArrayList<String> tempErrorList = new ArrayList<>();
        if (!EMAIL_REGEX_PATTERN.matcher(info.get(0)).matches()) {
            tempErrorList.add("Invalid email format");
        }
        return tempErrorList;
    }

    public static void mainCustomerPage() {
        int line = 0;
        JFrame frame = new JFrame("Message Sender");
        JPanel mainPanel = new JPanel(new GridBagLayout());

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridy = line++;
        gridBagConstraints.anchor = GridBagConstraints.WEST; // Set anchor to the left

        // Add display text with titles at the top on thr;ee different lines using HTML formatting
        //JLabel themeLabel = new JLabel("<html><div style='text-align: left;'>Current User<br>Seller<br>Existing Conversation</div></html>");
        JLabel currentUserLabel = new JLabel(String.format("Current User: %s (%s)", currentUser.split(",")[0], currentUser.split(",")[1]));
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new Insets(5, 10, 5, 10); // Add some padding
        mainPanel.add(currentUserLabel, gridBagConstraints);



        int counter = 0;
        String sellerStore = "Seller and stores: ";
        if (!sellerName.isEmpty() && !sellerName.get(0).isEmpty()) {
            JLabel sellerStorelabel = new JLabel(sellerStore);
            gridBagConstraints.gridy = line++;
            gridBagConstraints.gridwidth = 2;
            gridBagConstraints.insets = new Insets(5, 10, 5, 10); // Add some padding
            mainPanel.add(sellerStorelabel, gridBagConstraints);
            while (counter < sellerName.size()) {
                sellerStore = String.format("Seller: %s -> Stores: %s",sellerName.get(counter), storeName.get(counter));
                sellerStorelabel = new JLabel(sellerStore);
                gridBagConstraints.gridy = line++;
                gridBagConstraints.gridwidth = 2;
                gridBagConstraints.insets = new Insets(5, 10, 5, 10); // Add some padding
                mainPanel.add(sellerStorelabel, gridBagConstraints);
                counter++;
            }
        } else {
            sellerStore = "Server contain no seller";
            JLabel sellerStorelabel = new JLabel(sellerStore);
            gridBagConstraints.gridy = line++;
            gridBagConstraints.gridwidth = 2;
            gridBagConstraints.insets = new Insets(5, 10, 5, 10); // Add some padding
            mainPanel.add(sellerStorelabel, gridBagConstraints);
        }

        String conversations = "Exist Conversation: ";
        if (conversationUserList.isEmpty() || conversationUserList.get(0).isEmpty()) {
            conversations = "No exist conversation";
        } else {
            conversations += String.join(", ", conversationUserList);
        }

        JLabel conversationLabel = new JLabel(conversations);
        gridBagConstraints.gridy = line++;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new Insets(5, 10, 5, 10); // Add some padding
        mainPanel.add(conversationLabel, gridBagConstraints);

        JButton[] messageButtons = new JButton[9];

        // Define some fancy colors
        Color[] buttonColors = {
                new Color(237, 101, 102),
                new Color(102, 237, 141),
                new Color(102, 133, 237),
                new Color(237, 178, 102),
                new Color(204, 102, 237),
                new Color(180, 225, 102),
                new Color(177, 102, 237),
                new Color(237, 102, 204),
                new Color(50, 50, 204)
        };

        // Create a fancy border for the buttons
        Border fancyBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK, 5),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        );
        String[] optionText = {"Select a specific conversation to message",
                "Search user to create new conversation", "Block any user",
                "Unblock any user", "Invisible any user", "Indivisibles any user",
                "Account Modification", "add store", "log off"};
        for (int i = 0; i < messageButtons.length; i++) {
            messageButtons[i] = new JButton(optionText[i]);
            messageButtons[i].setBackground(buttonColors[i]); // Set different colors for each button
            messageButtons[i].setForeground(Color.WHITE); // Set white text color for better visibility
            messageButtons[i].setBorder(fancyBorder); // Apply the fancy border to buttons
            gridBagConstraints.gridy = i + line;
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridwidth = 1;
            mainPanel.add(messageButtons[i], gridBagConstraints);
        }

        // Create a fancy border for the whole frame
        Border frameBorder = BorderFactory.createLineBorder(Color.BLACK, 10);
        mainPanel.setBorder(BorderFactory.createCompoundBorder(frameBorder,
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        frame.add(mainPanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setVisible(true);

        for (int i = 0; i < messageButtons.length; i++) {
            int finalI = i;
            messageButtons[i].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    synchronized (LOCK) {
                        OPTION_PASS = finalI;
                        LOCK.notify();
                        frame.dispose();
                    }
                }
            });
        }

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                synchronized (LOCK) {
                    int result = JOptionPane.showConfirmDialog(frame,
                            "Are you sure you want to close the window?", "Confirm Close",
                            JOptionPane.YES_NO_OPTION);
                    if (result == JOptionPane.YES_OPTION) {
                        isFrameDisposed = true;
                        LOCK.notify();
                        frame.dispose(); // Close the frame
                    }
                }
            }
        });
    }

    public static void mainSellerPage() {
        JFrame frame = new JFrame("Message Sender");
        JPanel mainPanel = new JPanel(new GridBagLayout());

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST; // Set anchor to the left

        // Add display text with titles at the top on thr;ee different lines using HTML formatting
        //JLabel themeLabel = new JLabel("<html><div style='text-align: left;'>Current User<br>Seller<br>Existing Conversation</div></html>");
        JLabel currentUserLabel = new JLabel(String.format("Current User: %s (%s)",
                currentUser.split(",")[0], currentUser.split(",")[1]));
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new Insets(5, 10, 5, 10); // Add some padding
        mainPanel.add(currentUserLabel, gridBagConstraints);

        String sellerStore = "My stores:  \n";
        if (storeName.isEmpty() || storeName.get(0).isEmpty()) {
            sellerStore += "None";
        } else {
            sellerStore += String.join(", ", storeName);
        }

        JLabel storeLabel = new JLabel(sellerStore);
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new Insets(5, 10, 5, 10); // Add some padding
        mainPanel.add(storeLabel, gridBagConstraints);

        String conversations = "Exist Conversation: ";
        if (conversationUserList.isEmpty() || conversationUserList.get(0).isEmpty()) {
            conversations = "No exist conversation";
        } else {
            conversations += String.join(", ", conversationUserList);
        }

        JLabel conversationLabel = new JLabel(conversations);
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new Insets(5, 10, 5, 10); // Add some padding
        mainPanel.add(conversationLabel, gridBagConstraints);

        JButton[] messageButtons = new JButton[9];

        // Define some fancy colors
        Color[] buttonColors = {
                new Color(237, 101, 102),
                new Color(102, 237, 141),
                new Color(102, 133, 237),
                new Color(237, 178, 102),
                new Color(204, 102, 237),
                new Color(180, 225, 102),
                new Color(177, 102, 237),
                new Color(237, 102, 204),
                new Color(50, 50, 204)
        };

        // Create a fancy border for the buttons
        Border fancyBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK, 5),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        );

        String[] optionText = {"Select a specific conversation to message",
                "Search user to create new conversation", "Block any user",
                "Unblock any user", "Invisible any user", "Indivisibles any user",
                "Account Modification", "add store", "log off"};
        for (int i = 0; i < messageButtons.length; i++) {
            messageButtons[i] = new JButton(optionText[i]);
            messageButtons[i].setBackground(buttonColors[i]); // Set different colors for each button
            messageButtons[i].setForeground(Color.WHITE); // Set white text color for better visibility
            messageButtons[i].setBorder(fancyBorder); // Apply the fancy border to buttons
            gridBagConstraints.gridy = i + 3;
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridwidth = 1;
            mainPanel.add(messageButtons[i], gridBagConstraints);
        }

        // Create a fancy border for the whole frame
        Border frameBorder = BorderFactory.createLineBorder(Color.BLACK, 10);
        mainPanel.setBorder(BorderFactory.createCompoundBorder(frameBorder,
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        frame.add(mainPanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setVisible(true);

        for (int i = 0; i < messageButtons.length; i++) {
            int finalI = i;
            messageButtons[i].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    synchronized (LOCK) {
                        OPTION_PASS = finalI;
                        LOCK.notify();
                        frame.dispose();
                    }
                }
            });
        }

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                synchronized (LOCK) {
                    int result = JOptionPane.showConfirmDialog(frame, "Are you sure you want to close the window?",
                            "Confirm Close", JOptionPane.YES_NO_OPTION);
                    if (result == JOptionPane.YES_OPTION) {
                        isFrameDisposed = true;
                        LOCK.notify();
                        frame.dispose(); // Close the frame
                    }
                }
            }
        });
    }

    public static void messagePage() {
        JFrame frame = new JFrame("User Info");
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gridBagConstraints = new GridBagConstraints();

        boolean newLine = false;
        String[] tempArray = messageList.get(0).split("-");
        String name1 = tempArray[1];
        for (int i = 0; i < messageList.size(); i++) {
            int counter = i * 3;
            tempArray = messageList.get(i).split("-");
            String tempName = tempArray[1];
            String assemblyString = "";
            if (tempName.equals(name1)) {
                assemblyString = tempArray[0] + " " + tempArray[1];
                gridBagConstraints.gridx = 0;
            } else {
                assemblyString = tempArray[1] + " " + tempArray[0];
                gridBagConstraints.gridx = 1;
            }
            gridBagConstraints.gridy = counter;
            panel.add(new JLabel(assemblyString), gridBagConstraints);
            gridBagConstraints.gridy = counter + 1;
            String content = tempArray[2];
            panel.add(new JLabel(content), gridBagConstraints);
            gridBagConstraints.gridy = counter + 2;
            panel.add(new JLabel("\n"), gridBagConstraints);
        }
        frame.add(panel);
        frame.setSize(600, 750);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setLocation( 300, 200);
        new Thread(() -> {
            try {
                synchronized (LOCK) {
                    LOCK.wait();
                }
                frame.dispose();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    public static void main(String[] args) throws IOException {
        Scanner scan = new Scanner(System.in);
        int counter = 0;
        int userInputInt = 0;
        String userInputString = "";
        boolean error = false;
        boolean seller = false;
        boolean keepOption = true;
        boolean keepConversation = true;
        String tempString = "";
        String clientInput = "";
        ArrayList<String> userNameList = new ArrayList<>();
        ArrayList<String> userNameList2 = new ArrayList<>();
        ArrayList<String> storeNameList = new ArrayList<>();
        ArrayList<String> conversationList = new ArrayList<>();
        ArrayList<String> tempMessageList = new ArrayList<>();
        ArrayList<String> infoList = new ArrayList<>();

        // Connect to server
        JOptionPane.showMessageDialog(null, "Establishing connection",
                "Connecting", JOptionPane.INFORMATION_MESSAGE);
        try {
            Socket socket = new Socket(InetAddress.getLocalHost().getHostName(), 4242);
            JOptionPane.showMessageDialog(null, "Connect successfully",
                    GUI_TITLE, JOptionPane.INFORMATION_MESSAGE);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream());

            Matcher matcher;
            // Start program
            do {
                userInputInt = -1;
                userInputInt = JOptionPane.showConfirmDialog(null,
                        "Hello! Would you like to use the program?", GUI_TITLE, JOptionPane.YES_NO_OPTION);
                if (userInputInt == -1 || userInputInt == 1) {
                    endProgramDialog();
                    return;
                }
                writer.println("Start system");
                writer.flush();

                // Login page
                boolean notloggedIn;

                do {
                    String[] buttonOptions = {"Log in", "Create account"};
                    userInputInt = -1;
                    userInputInt = JOptionPane.showOptionDialog(null, "Choose an option:",
                            "StartPage", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                            null, buttonOptions, buttonOptions[0]
                    );
                    // login page
                    if (userInputInt == -1) {
                        endProgramDialog();
                        return;
                    }
                    notloggedIn = true;

                    isFrameDisposed = false;
                    if (userInputInt == 1) {
                        //create account
                        writer.println("Create account");
                        writer.flush();
                        SwingUtilities.invokeLater(Client::createAccountPage);
                    } else if (userInputInt == 0) {
                        // log in
                        writer.println("Log in");
                        writer.flush();
                        SwingUtilities.invokeLater(Client::loginPage);
                    }
                    synchronized (LOCK) {
                        try {
                            LOCK.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if (isFrameDisposed) {
                        endProgramDialog();
                        return;
                    }
                    writer.println(GUI_PASS);
                    writer.flush();

                    clientInput = reader.readLine();
                    if (clientInput.equals("fail")) {
                        JOptionPane.showMessageDialog(null, "Invalid email or password",
                                GUI_TITLE, JOptionPane.INFORMATION_MESSAGE);
                    } else if (clientInput.equals("User Already exist")) {
                        JOptionPane.showMessageDialog(null, "User exists already.",
                                GUI_TITLE, JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(null, "Successfully logged in",
                                GUI_TITLE, JOptionPane.INFORMATION_MESSAGE);
                        notloggedIn = false;
                    }
                } while (notloggedIn);

                do {
                    // Display
                    seller = true;
                    storeName = new ArrayList<String>();
                    sellerName = new ArrayList<String>();
                    conversationUserList = new ArrayList<String>();
                    storeNameList.clear();
                    userNameList.clear();
                    userNameList2.clear();
                    clientInput = reader.readLine();
                    if (clientInput.equals("Customer display")) {
                        seller = false;
                        clientInput = reader.readLine();
                        Arrays.stream(clientInput.split(";"))
                                .map(String::trim)
                                .forEach(storeName::add);
                        clientInput = reader.readLine();
                        Arrays.stream(clientInput.split(";"))
                                .map(String::trim)
                                .forEach(sellerName::add);

                    } else {
                        clientInput = reader.readLine();
                        Arrays.stream(clientInput.split(";"))
                                .map(String::trim)
                                .forEach(storeName::add);
                    }
                    clientInput = reader.readLine();
                    Arrays.stream(clientInput.split(";"))
                            .map(String::trim)
                            .forEach(conversationUserList::add);
                    counter = 0;
                    currentUser = reader.readLine();
                    if (!seller) {
                        SwingUtilities.invokeLater(Client::mainCustomerPage);
                    } else {
                        SwingUtilities.invokeLater(Client::mainSellerPage);
                    }
                    synchronized (LOCK) {
                        try {
                            LOCK.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if (isFrameDisposed) {
                        endProgramDialog();
                        return;
                    }
                    writer.println(OPTION_PASS);
                    writer.flush();
                    keepOption = true;
                    switch (OPTION_PASS) {
                        case 8 -> {
                            JOptionPane.showMessageDialog(null, "Logging off",
                                    GUI_TITLE, JOptionPane.INFORMATION_MESSAGE);
                            keepOption = false;
                        }
                        case 7 -> {
                            clientInput = reader.readLine();
                            if (clientInput.equals("Not seller")) {
                                JOptionPane.showMessageDialog(null, "Your are not a seller",
                                        GUI_TITLE, JOptionPane.INFORMATION_MESSAGE);
                            } else {
                                storeNameList.clear();
                                clientInput = reader.readLine();
                                if (!clientInput.isEmpty()) {
                                    Arrays.stream(clientInput.split(";"))
                                            .map(String::trim)
                                            .forEach(storeNameList::add);
                                    JOptionPane.showMessageDialog(null, String.format(
                                                    "Stores you own: %s", String.join(";", storeNameList)),
                                            GUI_TITLE, JOptionPane.INFORMATION_MESSAGE);
                                } else {
                                    JOptionPane.showMessageDialog(null,
                                            "You don't have any store", GUI_TITLE,
                                            JOptionPane.INFORMATION_MESSAGE);
                                }
                                do {
                                    error = false;
                                    userInputString = JOptionPane.showInputDialog(null,
                                            "Enter the store you want to add",
                                            GUI_TITLE, JOptionPane.QUESTION_MESSAGE);
                                    if (userInputString == null) {
                                        endProgramDialog();
                                        return;
                                    }
                                    if (userInputString.isEmpty()) {
                                        JOptionPane.showMessageDialog(null,
                                                "Store name cannot be empty", GUI_TITLE,
                                                JOptionPane.INFORMATION_MESSAGE);
                                        error = true;
                                    }
                                } while (error);
                                writer.println(userInputString);
                                writer.flush();
                                JOptionPane.showMessageDialog(null,
                                        "Successfully added a store", GUI_TITLE,
                                        JOptionPane.INFORMATION_MESSAGE);
                            }
                        }
                        case 6 -> {
                            error = false;
                            String[] accountModificationText = {"Change name", "Change email",
                                    "Change password", "Delete Account"};
                            userInputString = (String) JOptionPane.showInputDialog(null,
                                    "Please select option from the list ", GUI_TITLE,
                                    JOptionPane.QUESTION_MESSAGE, null, accountModificationText, null);
                            if (tempString == null) {
                                endProgramDialog();
                                return;
                            }
                            switch (userInputString) {
                                case "Change name" -> {tempString = "name"; userInputInt = 0;}
                                case "Change email" -> {tempString = "email"; userInputInt = 1;}
                                case "Change password" -> {tempString = "password"; userInputInt = 2;}
                                case "Delete Account" -> {
                                    JOptionPane.showMessageDialog(null,
                                            "logging off",
                                            GUI_TITLE, JOptionPane.INFORMATION_MESSAGE);
                                    keepOption = false;
                                    userInputInt = 3;
                                }
                            }
                            writer.println(userInputInt);
                            writer.flush();
                            if (userInputInt != 3) {
                                do {
                                    error = false;
                                    userInputString = JOptionPane.showInputDialog(null,
                                            String.format("What is the new %s", tempString),
                                            GUI_TITLE, JOptionPane.QUESTION_MESSAGE);
                                    if (tempString.equals("name") || tempString.equals("email")) {
                                        if (tempString.equals("name")) {
                                            if (!Arrays.stream(userInputString.split("\\s+")).
                                                    allMatch(word -> word.matches(NAME_PATTERN))) {
                                                JOptionPane.showMessageDialog(null,
                                                        "Invalid name format",
                                                        GUI_TITLE, JOptionPane.INFORMATION_MESSAGE);
                                                error = true;
                                            }
                                        }
                                        if (tempString.equals("email")) {
                                            matcher = EMAIL_REGEX_PATTERN.matcher(userInputString);
                                            if (!matcher.matches()) {
                                                JOptionPane.showMessageDialog(null,
                                                        "Invalid email format",
                                                        GUI_TITLE, JOptionPane.INFORMATION_MESSAGE);
                                                error = true;
                                            }
                                        }
                                    }
                                    if (userInputString == null) {
                                        endProgramDialog();
                                        return;
                                    }
                                    if (userInputString.isEmpty()) {
                                        JOptionPane.showMessageDialog(null,
                                                String.format("%s cannot be empty", tempString),
                                                GUI_TITLE, JOptionPane.INFORMATION_MESSAGE);
                                        error = true;
                                    }
                                } while (error);
                                writer.println(userInputString);
                                writer.flush();
                            }
                            clientInput = reader.readLine();
                            JOptionPane.showMessageDialog(null,
                                    clientInput,
                                    GUI_TITLE, JOptionPane.INFORMATION_MESSAGE);
                        }
                        case 5 -> {
                            clientInput = reader.readLine();
                            if (clientInput.equals("No result")) {
                                JOptionPane.showMessageDialog(null,
                                        "No user been invisible by you", GUI_TITLE,
                                        JOptionPane.INFORMATION_MESSAGE);
                            } else {
                                userNameList.clear();
                                Arrays.stream(clientInput.split(";"))
                                        .map(String::trim)
                                        .forEach(userNameList::add);
                                JOptionPane.showMessageDialog(null, String.format("Invisible Users: %s",
                                                String.join(";", userNameList)),
                                        GUI_TITLE, JOptionPane.INFORMATION_MESSAGE);
                                userInputString = (String) JOptionPane.showInputDialog(null,
                                        "Select an user to indivisibles from the list ", GUI_TITLE,
                                        JOptionPane.QUESTION_MESSAGE, null, userNameList.toArray(),
                                        userNameList.get(0));
                                if (userInputString == null) {
                                    endProgramDialog();
                                    return;
                                }
                                writer.println(userInputString);
                                writer.flush();
                                clientInput = reader.readLine();
                                if (!clientInput.equals("fail")) {
                                    JOptionPane.showMessageDialog(null,
                                            String.format("Successfully indivisible %s with %s",
                                                    userInputString.substring(userInputString.indexOf(",") + 1),
                                                    userInputString.substring(0, userInputString.indexOf(","))),
                                            GUI_TITLE, JOptionPane.INFORMATION_MESSAGE);
                                } else {
                                    JOptionPane.showMessageDialog(null,
                                            String.format("Fail to indivisible %s with %s",
                                                    userInputString.substring(userInputString.indexOf(",") + 1),
                                                    userInputString.substring(0, userInputString.indexOf(","))),
                                            GUI_TITLE, JOptionPane.INFORMATION_MESSAGE);
                                }
                            }
                        }
                        case 4 -> {
                            do {
                                error = false;
                                userInputString = JOptionPane.showInputDialog(null,
                                        "What is the name of the user you want to invisible?",
                                        GUI_TITLE, JOptionPane.QUESTION_MESSAGE);
                                if (userInputString == null) {
                                    endProgramDialog();
                                    return;
                                }
                                if (userInputString.isEmpty()) {
                                    JOptionPane.showMessageDialog(null,
                                            "Name cannot be empty",
                                            GUI_TITLE, JOptionPane.INFORMATION_MESSAGE);
                                    error = true;
                                }
                            } while (error);
                            writer.println(userInputString);
                            writer.flush();
                            clientInput = reader.readLine();
                            if (clientInput.equals("No result")) {
                                JOptionPane.showMessageDialog(null,
                                        "No user that contain the name",
                                        GUI_TITLE, JOptionPane.INFORMATION_MESSAGE);
                            } else {
                                userNameList.clear();
                                Arrays.stream(clientInput.split(";"))
                                        .map(String::trim)
                                        .forEach(userNameList::add);
                                userInputString = (String) JOptionPane.showInputDialog(null,
                                        "Please select an user from the list ", GUI_TITLE,
                                        JOptionPane.QUESTION_MESSAGE, null, userNameList.toArray(),
                                        userNameList.get(0));
                                if (userInputString == null) {
                                    endProgramDialog();
                                    return;
                                }
                                writer.println(userInputString);
                                writer.flush();
                                clientInput = reader.readLine();
                                if (clientInput.equals("fail")) {
                                    JOptionPane.showMessageDialog(null,
                                            String.format("Fail to invisible %s with %s",
                                                    userInputString.substring(userInputString.indexOf(",") + 1),
                                                    userInputString.substring(0, userInputString.indexOf(","))),
                                            GUI_TITLE, JOptionPane.INFORMATION_MESSAGE);

                                } else if (clientInput.equals("Already invisible this user")) {
                                    JOptionPane.showMessageDialog(null,
                                            String.format("Already invisible %s with %s",
                                                    userInputString.substring(userInputString.indexOf(",") + 1),
                                                    userInputString.substring(0, userInputString.indexOf(","))),
                                            GUI_TITLE, JOptionPane.INFORMATION_MESSAGE);
                                } else {
                                    JOptionPane.showMessageDialog(null,
                                            String.format("Successfully invisible %s with %s",
                                                    userInputString.substring(userInputString.indexOf(",") + 1),
                                                    userInputString.substring(0, userInputString.indexOf(","))),
                                            GUI_TITLE, JOptionPane.INFORMATION_MESSAGE);
                                }
                            }
                        }
                        case 3 -> {
                            clientInput = reader.readLine();
                            if (clientInput.equals("No result")) {
                                JOptionPane.showMessageDialog(null, "No user been blocked by you",
                                        GUI_TITLE, JOptionPane.INFORMATION_MESSAGE);
                            } else {
                                userNameList.clear();
                                Arrays.stream(clientInput.split(";"))
                                        .map(String::trim)
                                        .forEach(userNameList::add);
                                JOptionPane.showMessageDialog(null, String.format("Blocked Users: %s",
                                                String.join(";", userNameList)),
                                        GUI_TITLE, JOptionPane.INFORMATION_MESSAGE);
                                userInputString = (String) JOptionPane.showInputDialog(null,
                                        "Select an user to unblocks from the list ", GUI_TITLE,
                                        JOptionPane.QUESTION_MESSAGE, null, userNameList.toArray(),
                                        userNameList.get(0));
                                if (userInputString == null) {
                                    endProgramDialog();
                                    return;
                                }
                                writer.println(userInputString);
                                writer.flush();
                                clientInput = reader.readLine();
                                if (!clientInput.equals("fail")) {
                                    JOptionPane.showMessageDialog(null,
                                            String.format("Successfully unblocked %s with %s",
                                                    userInputString.substring(userInputString.indexOf(",") + 1),
                                                    userInputString.substring(0, userInputString.indexOf(","))),
                                            GUI_TITLE, JOptionPane.INFORMATION_MESSAGE);
                                } else {
                                    JOptionPane.showMessageDialog(null,
                                            String.format("Fail to unblock %s with %s",
                                                    userInputString.substring(userInputString.indexOf(",") + 1),
                                                    userInputString.substring(0, userInputString.indexOf(","))),
                                            GUI_TITLE, JOptionPane.INFORMATION_MESSAGE);
                                }
                            }
                        }
                        case 2 -> {
                            do {
                                error = false;
                                userInputString = JOptionPane.showInputDialog(null,
                                        "What is the name of the user you want to block?",
                                        GUI_TITLE, JOptionPane.QUESTION_MESSAGE);
                                if (userInputString == null) {
                                    endProgramDialog();
                                    return;
                                }
                                if (userInputString.isEmpty()) {
                                    JOptionPane.showMessageDialog(null,
                                            "Name cannot be empty",
                                            GUI_TITLE, JOptionPane.INFORMATION_MESSAGE);
                                    error = true;
                                }
                            } while (error);
                            writer.println(userInputString);
                            writer.flush();
                            clientInput = reader.readLine();
                            if (clientInput.equals("No result")) {
                                JOptionPane.showMessageDialog(null,
                                        "No user that contain the name",
                                        GUI_TITLE, JOptionPane.INFORMATION_MESSAGE);
                            } else {
                                userNameList.clear();
                                Arrays.stream(clientInput.split(";"))
                                        .map(String::trim)
                                        .forEach(userNameList::add);

                                userInputString = (String) JOptionPane.showInputDialog(null,
                                        "Please select an user from the list ", GUI_TITLE,
                                        JOptionPane.QUESTION_MESSAGE, null, userNameList.toArray(),
                                        userNameList.get(0));
                                if (userInputString == null) {
                                    endProgramDialog();
                                    return;
                                }
                                writer.println(userInputString);
                                writer.flush();
                                clientInput = reader.readLine();
                                if (clientInput.equals("fail")) {
                                    JOptionPane.showMessageDialog(null,
                                            String.format("Fail to block %s with %s",
                                                    userInputString.substring(userInputString.indexOf(",") + 1),
                                                    userInputString.substring(0, userInputString.indexOf(","))),
                                            GUI_TITLE, JOptionPane.INFORMATION_MESSAGE);

                                } else if (clientInput.equals("Already blocked this user")) {
                                    JOptionPane.showMessageDialog(null,
                                            String.format("Already blocked %s with %s",
                                                    userInputString.substring(userInputString.indexOf(",") + 1),
                                                    userInputString.substring(0, userInputString.indexOf(","))),
                                            GUI_TITLE, JOptionPane.INFORMATION_MESSAGE);
                                } else {
                                    JOptionPane.showMessageDialog(null,
                                            String.format("Successfully blocked %s with %s",
                                                    userInputString.substring(userInputString.indexOf(",") + 1),
                                                    userInputString.substring(0, userInputString.indexOf(","))),
                                            GUI_TITLE, JOptionPane.INFORMATION_MESSAGE);
                                }
                            }
                        }
                        case 1 -> {
                            do {
                                error = false;
                                userInputString = JOptionPane.showInputDialog(null,
                                        "What is the name of the user you want" +
                                                " to create new conversation? (Search)",
                                        GUI_TITLE, JOptionPane.QUESTION_MESSAGE);
                                if (userInputString == null) {
                                    endProgramDialog();
                                    return;
                                }
                                if (userInputString.isEmpty()) {
                                    JOptionPane.showMessageDialog(null,
                                            "Name cannot be empty",
                                            GUI_TITLE, JOptionPane.INFORMATION_MESSAGE);
                                    error = true;
                                }
                            } while (error);
                            writer.println(userInputString);
                            writer.flush();
                            clientInput = reader.readLine();
                            if (clientInput.equals("No result")) {
                                JOptionPane.showMessageDialog(null,
                                        "No user that contain the name",
                                        GUI_TITLE, JOptionPane.INFORMATION_MESSAGE);
                            } else {
                                userNameList.clear();
                                Arrays.stream(clientInput.split(";"))
                                        .map(String::trim)
                                        .forEach(userNameList::add);
                                userInputString = (String) JOptionPane.showInputDialog(null,
                                        "Please select an user from the list to create conversation", GUI_TITLE,
                                        JOptionPane.QUESTION_MESSAGE, null, userNameList.toArray(),
                                        userNameList.get(0));
                                if (userInputString == null) {
                                    endProgramDialog();
                                    return;
                                }
                                writer.println(userInputString);
                                writer.flush();
                                clientInput = reader.readLine();
                                if (clientInput.equals("fail")) {
                                    JOptionPane.showMessageDialog(null,
                                            String.format("Fail to start a conversation to %s with %s",
                                                    userInputString.substring(userInputString.indexOf(",") + 1),
                                                    userInputString.substring(0, userInputString.indexOf(","))),
                                            GUI_TITLE, JOptionPane.INFORMATION_MESSAGE);
                                } else if (clientInput.equals("Conversation exist")) {
                                    JOptionPane.showMessageDialog(null,
                                            String.format("Already have a conversation with %s with %s",
                                                    userInputString.substring(userInputString.indexOf(",") + 1),
                                                    userInputString.substring(0, userInputString.indexOf(","))),
                                            GUI_TITLE, JOptionPane.INFORMATION_MESSAGE);
                                } else {
                                    JOptionPane.showMessageDialog(null,
                                            String.format("Successfully start a conversation to %s with %s",
                                                    userInputString.substring(userInputString.indexOf(",") + 1),
                                                    userInputString.substring(0, userInputString.indexOf(","))),
                                            GUI_TITLE, JOptionPane.INFORMATION_MESSAGE);
                                }
                            }
                        }
                        case 0 -> {
                            clientInput = reader.readLine();
                            if (!clientInput.equals("No conversation")) {
                                conversationList.clear();
                                Arrays.stream(clientInput.split(";"))
                                        .map(String::trim)
                                        .forEach(conversationList::add);

                                userInputString = (String) JOptionPane.showInputDialog(null,
                                        "Select a exist conversation", GUI_TITLE,
                                        JOptionPane.QUESTION_MESSAGE, null, conversationList.toArray(), null);
                                if (userInputString == null) {
                                    endProgramDialog();
                                    return;
                                }
                                writer.println(userInputString);
                                writer.flush();
                                clientInput = reader.readLine();
                                if (clientInput.equals("fail")) {
                                    JOptionPane.showMessageDialog(null,
                                            "Incorrect email and name",
                                            GUI_TITLE, JOptionPane.INFORMATION_MESSAGE);
                                } else {
                                    do {
                                        keepConversation = true;
                                        clientInput = reader.readLine();
                                        if (clientInput.equals("fail")) {
                                            JOptionPane.showMessageDialog(null,
                                                    "No message",
                                                    GUI_TITLE, JOptionPane.INFORMATION_MESSAGE);
                                        } else {
                                            // Display message
                                            messageList.clear();
                                            Arrays.stream(clientInput.split(";"))
                                                    .map(String::trim)
                                                    .forEach(messageList::add);
                                            SwingUtilities.invokeLater(Client::messagePage);
                                        }
                                        String[] accountModificationText = {"0: New message", "1: Edit Message",
                                                "2: DeleteMessage", "3: Exit conversation"};
                                        userInputString = (String) JOptionPane.showInputDialog(null,
                                                "Please select option from the list", GUI_TITLE,
                                                JOptionPane.QUESTION_MESSAGE, null, accountModificationText, null);
                                        if (userInputString == null) {
                                            synchronized (LOCK) {
                                                LOCK.notify();
                                            }
                                            endProgramDialog();
                                            return;
                                        }
                                        switch (userInputString) {
                                            case "0: New message" -> userInputInt = 0;
                                            case "1: Edit Message" -> userInputInt = 1;
                                            case "2: DeleteMessage" -> userInputInt = 2;
                                            case "3: Exit conversation" -> {
                                                synchronized (LOCK) {
                                                    LOCK.notify();
                                                }
                                                keepConversation = false;
                                                userInputInt = 3;
                                            }
                                        }
                                        writer.println(userInputInt);
                                        writer.flush();
                                        switch (userInputInt) {
                                            case 0 -> {
                                                do {
                                                    error = false;
                                                    userInputString = JOptionPane.showInputDialog(null,
                                                            "New message content",
                                                            GUI_TITLE, JOptionPane.QUESTION_MESSAGE);
                                                    if (userInputString == null) {
                                                        endProgramDialog();
                                                        return;
                                                    }
                                                    if (userInputString.isEmpty()) {
                                                        JOptionPane.showMessageDialog(null,
                                                                "Content cannot be empty",
                                                                GUI_TITLE, JOptionPane.INFORMATION_MESSAGE);

                                                        error = true;
                                                    }
                                                } while (error);
                                                writer.println(userInputString);
                                                writer.flush();
                                                clientInput = reader.readLine();
                                                if (clientInput.equals("blocked")) {
                                                    JOptionPane.showMessageDialog(null,
                                                            "You are blocked by receiver," +
                                                                    " cannot send message",
                                                            GUI_TITLE, JOptionPane.INFORMATION_MESSAGE);
                                                }
                                                synchronized (LOCK) {
                                                    LOCK.notify();
                                                }
                                            }
                                            case 1 -> {
                                                do {
                                                    error = false;
                                                    userInputString = JOptionPane.showInputDialog(null,
                                                            "What is The old message content you want to edit",
                                                            GUI_TITLE, JOptionPane.QUESTION_MESSAGE);
                                                    if (userInputString == null) {
                                                        synchronized (LOCK) {
                                                            LOCK.notify();
                                                        }
                                                        endProgramDialog();
                                                        return;
                                                    }
                                                    if (userInputString.isEmpty()) {
                                                        JOptionPane.showMessageDialog(null,
                                                                "Content cannot be empty",
                                                                GUI_TITLE, JOptionPane.INFORMATION_MESSAGE);

                                                        error = true;
                                                    }
                                                } while (error);
                                                tempString = userInputString + ";";
                                                writer.println(userInputString);
                                                writer.flush();
                                                clientInput = reader.readLine();
                                                if (clientInput.equals("No result")) {
                                                    JOptionPane.showMessageDialog(null,
                                                            "No such message content find",
                                                            GUI_TITLE, JOptionPane.INFORMATION_MESSAGE);
                                                } else {
                                                    tempMessageList.clear();
                                                    Arrays.stream(clientInput.split(";"))
                                                            .map(String::trim)
                                                            .forEach(tempMessageList::add);
                                                    //tempString += userInputString + ";";
                                                    do {
                                                        error = false;
                                                        userInputString = (String) JOptionPane.showInputDialog(null,
                                                                "Please select a message from the list", GUI_TITLE,
                                                                JOptionPane.QUESTION_MESSAGE, null, tempMessageList.toArray(), null);
                                                        if (userInputString == null) {
                                                            endProgramDialog();
                                                            return;
                                                        }
                                                        if (userInputString.isEmpty()) {
                                                            JOptionPane.showMessageDialog(null,
                                                                    "Time cannot be empty",
                                                                    GUI_TITLE, JOptionPane.INFORMATION_MESSAGE);

                                                            error = true;
                                                        }
                                                    } while (error);
                                                    tempString += userInputString.substring(0, userInputString.indexOf("-")) + ";";
                                                    do {
                                                        error = false;
                                                        userInputString = JOptionPane.showInputDialog(null,
                                                                "New message content",
                                                                GUI_TITLE, JOptionPane.QUESTION_MESSAGE);
                                                        if (userInputString == null) {
                                                            endProgramDialog();
                                                            return;
                                                        }
                                                        if (userInputString.isEmpty()) {
                                                            JOptionPane.showMessageDialog(null,
                                                                    "Content cannot be empty",
                                                                    GUI_TITLE, JOptionPane.INFORMATION_MESSAGE);

                                                            error = true;
                                                        }
                                                    } while (error);
                                                    tempString += userInputString;
                                                    writer.println(tempString);
                                                    writer.flush();
                                                    clientInput = reader.readLine();
                                                    if (clientInput.equals("Message not found")) {
                                                        JOptionPane.showMessageDialog(null,
                                                                "Input message not found",
                                                                GUI_TITLE, JOptionPane.INFORMATION_MESSAGE);
                                                    }
                                                }

                                                synchronized (LOCK) {
                                                    LOCK.notify();
                                                }
                                            }
                                            case 2 -> {
                                                tempString = "";
                                                do {
                                                    error = false;
                                                    userInputString = JOptionPane.showInputDialog(null,
                                                            "The message content you want to delete",
                                                            GUI_TITLE, JOptionPane.QUESTION_MESSAGE);
                                                    if (userInputString == null) {
                                                        endProgramDialog();
                                                        return;
                                                    }
                                                    if (userInputString.isEmpty()) {
                                                        JOptionPane.showMessageDialog(null,
                                                                "Content cannot be empty",
                                                                GUI_TITLE, JOptionPane.INFORMATION_MESSAGE);

                                                        error = true;
                                                    }
                                                } while (error);
                                                writer.println(userInputString);
                                                writer.flush();
                                                clientInput = reader.readLine();
                                                if (clientInput.equals("No result")) {
                                                    JOptionPane.showMessageDialog(null,
                                                            "No such message content find",
                                                            GUI_TITLE, JOptionPane.INFORMATION_MESSAGE);
                                                } else {
                                                    tempMessageList.clear();
                                                    Arrays.stream(clientInput.split(";"))
                                                            .map(String::trim)
                                                            .forEach(tempMessageList::add);
                                                    do {
                                                        error = false;
                                                        userInputString = (String) JOptionPane.showInputDialog(null,
                                                                "Please select a message from the list", GUI_TITLE,
                                                                JOptionPane.QUESTION_MESSAGE, null, tempMessageList.toArray(), null);
                                                        if (userInputString == null) {
                                                            endProgramDialog();
                                                            return;
                                                        }
                                                        if (userInputString.isEmpty()) {
                                                            JOptionPane.showMessageDialog(null,
                                                                    "Time cannot be empty",
                                                                    GUI_TITLE, JOptionPane.INFORMATION_MESSAGE);

                                                            error = true;
                                                        }
                                                    } while (error);
                                                    tempString = userInputString;
                                                    writer.println(tempString);
                                                    writer.flush();
                                                    clientInput = reader.readLine();
                                                    if (clientInput.equals("Message not found")) {
                                                        JOptionPane.showMessageDialog(null,
                                                                "Input message not found",
                                                                GUI_TITLE, JOptionPane.INFORMATION_MESSAGE);
                                                    }
                                                }
                                                synchronized (LOCK) {
                                                    LOCK.notify();
                                                }
                                            }
                                            case 3 -> {
                                                JOptionPane.showMessageDialog(null,
                                                        "Exiting conversation",
                                                        GUI_TITLE, JOptionPane.INFORMATION_MESSAGE);
                                                keepConversation = false;
                                                synchronized (LOCK) {
                                                    LOCK.notify();
                                                }
                                            }
                                        }
                                    } while (keepConversation);
                                }
                            } else {
                                JOptionPane.showMessageDialog(null,
                                        "You have no exist conversation," +
                                                " please add new user to make conversation",
                                        GUI_TITLE, JOptionPane.INFORMATION_MESSAGE);
                            }
                        }
                    }
                } while (keepOption);
            } while (true);
        } catch (UnknownHostException | SocketException e) {
            JOptionPane.showMessageDialog(null, "Given host name and port number cannot" +
                    " establish connection with the server", "Disconnected", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}