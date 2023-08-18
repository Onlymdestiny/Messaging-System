import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
/**
 * Challenge 12 -- Paint.java
 *
 * This
 *
 * @author Riya Thomas, lab sec 817
 *
 * @version September 17, 2017
 *
 */
public class StartPage {
    public static void main(String[] args) {
        // Press Alt+Enter with your caret at the highlighted text to see how
        // IntelliJ IDEA suggests fixing it.
        System.out.printf("Hello and welcome!");

        // Press Shift+F10 or click the green arrow button in the gutter to run the code.

            // Press Shift+F9 to start debugging your code. We have set one breakpoint
            // for you, but you can always add more by pressing Ctrl+F8.


            JFrame frame = new JFrame("User Info");
            JPanel panel = new JPanel(new GridBagLayout());

            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.weightx = 0.001;
            gridBagConstraints.anchor = GridBagConstraints.LINE_START;

            gridBagConstraints.anchor = GridBagConstraints.LINE_END;
            JLabel username = new JLabel("Username ");
            panel.add(username, gridBagConstraints);
            gridBagConstraints.anchor = GridBagConstraints.LINE_START;
            JTextField nameText = new JTextField("",20);
            gridBagConstraints.gridx = 1;
            panel.add(nameText, gridBagConstraints);
            JLabel nameLabel = new JLabel();
            nameLabel.setVisible(false);
            panel.add(nameLabel, gridBagConstraints);

            gridBagConstraints.anchor = GridBagConstraints.LINE_END;
            JLabel password = new JLabel("Password ");
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 1;
            panel.add(password, gridBagConstraints);
            gridBagConstraints.anchor = GridBagConstraints.LINE_START;
            JTextField emailText = new JTextField(20);
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridwidth = 0;
            panel.add(emailText, gridBagConstraints);
            JLabel emailLabel = new JLabel();
            emailLabel.setVisible(false);
            panel.add(emailLabel, gridBagConstraints);


            gridBagConstraints.gridwidth = 6;
            JButton login = new JButton("Login");
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 2;
            JPanel panel2 = new JPanel();
            panel2.add(login, gridBagConstraints);

            // EDIT
            JButton createAccount = new JButton("Create Account");
            gridBagConstraints.gridx = 1;
            panel2.add(createAccount, gridBagConstraints);
            gridBagConstraints.weightx = 0;

            panel.add(panel2, gridBagConstraints);

            frame.add(panel);
            frame.setSize(500,400);
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);

            // SAVE BUTTON LISTENER
            login.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    nameText.setVisible(false);
                    emailText.setVisible(false);
                    nameLabel.setText(nameText.getText());
                    emailLabel.setText(emailText.getText());
                    nameLabel.setVisible(true);
                    emailLabel.setVisible(true);
                }
            });

            createAccount.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    nameText.setVisible(false);
                    emailText.setVisible(false);
                    nameLabel.setText(nameText.getText());
                    emailLabel.setText(emailText.getText());
                    nameLabel.setVisible(true);
                    emailLabel.setVisible(true);
                }
            });


    }
}