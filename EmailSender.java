import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class EmailSender {

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Email Sender");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        frame.getContentPane().add(panel);

        DefaultListModel<String> emailListModel = new DefaultListModel<>();
        JList<String> emailList = new JList<>(emailListModel);
        JScrollPane scrollPane = new JScrollPane(emailList);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        panel.add(buttonPanel, BorderLayout.SOUTH);

        JButton addButton = new JButton("Добавить");
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addEmail(emailListModel, frame);
            }
        });
        buttonPanel.add(addButton);

        JButton removeButton = new JButton("Удалить");
        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                removeEmail(emailList, emailListModel);
            }
        });
        buttonPanel.add(removeButton);

        JButton sendButton = new JButton("Отправить письма");
        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendEmails(emailListModel);
            }
        });
        buttonPanel.add(sendButton);

        loadEmailsFromFile("C://Users//mekai//IdeaProjects//untitled//src//emails.txt", emailListModel);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void addEmail(DefaultListModel<String> emailListModel, JFrame frame) {
        String email = JOptionPane.showInputDialog(frame, "Введите адрес электронной почты:");
        if (email != null && !email.trim().isEmpty()) {
            String name = JOptionPane.showInputDialog(frame, "Введите имя для этой почты:");
            if (name != null && !name.trim().isEmpty()) {
                emailListModel.addElement(email + "-" + name);
                saveEmailsToFile("C://Users//mekai//IdeaProjects//untitled//src//emails.txt", emailListModel);
            }
        }
    }

    private static void removeEmail(JList<String> emailList, DefaultListModel<String> emailListModel) {
        int selectedIndex = emailList.getSelectedIndex();
        if (selectedIndex != -1) {
            emailListModel.remove(selectedIndex);
            saveEmailsToFile("C://Users//mekai//IdeaProjects//untitled//src//emails.txt", emailListModel);
        }
    }

    private static void sendEmails(DefaultListModel<String> emailListModel) {
        try {
            BufferedReader cryptReader = new BufferedReader(new FileReader("C://Users//mekai//IdeaProjects//untitled//src//crypt.txt"));
            String encryptedCredentials = cryptReader.readLine();
            cryptReader.close();

            // Дешифровка данных с помощью шифра Цезаря
            int shift = 3;
            String decryptedCredentials = decrypt(encryptedCredentials, shift);

            // Разделение расшифрованной строки на почту и пароль
            String[] credentials = decryptedCredentials.split(" ");

            if (credentials.length >= 2) {
                final String username = credentials[0];
                final String password = credentials[1];

                String smtpHost = "smtp.mail.ru"; // Измените на адрес SMTP вашего почтового сервиса

                Properties props = new Properties();
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.host", smtpHost);
                props.put("mail.smtp.port", "587");

                Session session = Session.getInstance(props, new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

                String filePathMessage = "C://Users//mekai//IdeaProjects//untitled//src//text.txt"; // Путь к файлу с текстом сообщения

                BufferedReader reader = new BufferedReader(new FileReader(filePathMessage));
                StringBuilder messageContent = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    messageContent.append(line).append("\n");
                }
                reader.close();

                for (int i = 0; i < emailListModel.size(); i++) {
                    String[] parts = emailListModel.get(i).split("-");
                    if (parts.length >= 2) {
                        String email = parts[0];
                        String name = parts[1];

                        MimeMessage message = new MimeMessage(session);
                        message.setFrom(new InternetAddress(username));
                        message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));

                        String personalizedMessage = "Добрый день, " + name + "!\n" + messageContent.toString();

                        message.setSubject("Добрый день!");
                        message.setText(personalizedMessage);

                        Transport.send(message);
                        System.out.println("Сообщение успешно отправлено на " + email);
                    }
                }
            }
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
        }
    }

    private static String decrypt(String encryptedText, int shift) {
        StringBuilder decrypted = new StringBuilder();

        for (char c : encryptedText.toCharArray()) {
            if (Character.isLetter(c)) {
                char base = Character.isUpperCase(c) ? 'A' : 'a';
                decrypted.append((char) (((c - base - shift + 26) % 26) + base));
            } else {
                decrypted.append(c);
            }
        }

        return decrypted.toString();
    }

    private static void loadEmailsFromFile(String filePath, DefaultListModel<String> emailListModel) {
        try {
            BufferedReader recipientsReader = new BufferedReader(new FileReader(filePath));
            String recipient;
            while ((recipient = recipientsReader.readLine()) != null) {
                emailListModel.addElement(recipient);
            }
            recipientsReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void saveEmailsToFile(String filePath, DefaultListModel<String> emailListModel) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
            for (int i = 0; i < emailListModel.size(); i++) {
                writer.write(emailListModel.get(i));
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
