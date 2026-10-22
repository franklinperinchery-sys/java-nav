import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;

public class CineBookGUI extends JFrame {
    private static final long serialVersionUID = 1L;

    static final String[] SHOW_TIMES = {"Today 6:00 PM", "Today 9:00 PM", "Tomorrow 3:00 PM"};
    static final String[] THEATRES = {
        "PVR Lulu, Kochi", "Cinepolis Centre Square, Kochi", "Vanitha Vineetha, Edappally",
        "New Theatre, Thiruvananthapuram", "Ariesplex SL Cinemas, Thiruvananthapuram",
        "Aashirvad Cinemas, Kottayam", "Anaswara Theatre, Kottayam",
        "Abhilash Theatre, Changanassery", "Dhanya Remya, Thrissur", "Ragam Theatre, Thrissur",
        "Crown Theatre, Kozhikode", "Apsara Theatre, Kozhikode",
        "Pranav Theatre, Kollam", "Kairali Sree Theatre, Alappuzha"
    };

    CineBookController controller;

    JTabbedPane tabs = new JTabbedPane();
    JLabel accountLabel = new JLabel("Not signed in");
    JButton signOutBtn = new JButton("Sign out");

    JTextField loginEmail = new JTextField(18);
    JPasswordField loginPassword = new JPasswordField(18);
    JButton loginBtn = new JButton("Sign in");
    JLabel loginMsg = new JLabel(" ");
    JTextField regName = new JTextField(18), regEmail = new JTextField(18);
    JPasswordField regPassword = new JPasswordField(18);
    JButton regBtn = new JButton("Register");
    JLabel regMsg = new JLabel(" ");

    JTextField searchField = new JTextField(25);
    DefaultTableModel movieModel = makeModel(new String[]{"Title", "Genre", "Duration (min)", "Price (Rs.)"});
    JTable movieTable = new JTable(movieModel);

    JComboBox<String> movieCombo = new JComboBox<String>();
    JComboBox<String> theatreCombo = new JComboBox<String>(THEATRES);
    JComboBox<String> timeCombo = new JComboBox<String>(SHOW_TIMES);
    JToggleButton[] seatBtns = new JToggleButton[32];
    ButtonGroup seatGroup = new ButtonGroup();
    JButton confirmBtn = new JButton("Confirm booking");
    JLabel bookingMsg = new JLabel(" ");

    DefaultTableModel bookingModel = makeModel(
        new String[]{"ID", "Customer", "Movie", "Theatre", "Show", "Seat", "Status"});
    JTable bookingTable = new JTable(bookingModel);
    JButton refreshBtn = new JButton("Refresh");
    JButton cancelBtn = new JButton("Cancel selected booking");

    JTextField newTitle = new JTextField(), newGenre = new JTextField();
    JTextField newDuration = new JTextField("120"), newPrice = new JTextField("200");
    JButton addMovieBtn = new JButton("Add movie");
    JButton deleteMovieBtn = new JButton("Delete selected movie");
    JLabel adminMsg = new JLabel(" ");
    DefaultTableModel adminModel = makeModel(new String[]{"ID", "Title", "Genre", "Price (Rs.)"});
    JTable adminTable = new JTable(adminModel);

    CineBookGUI() {
        super("CineBook - Movie Tickets");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(980, 640);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel top = new JPanel(new BorderLayout());
        CineBookStyle.padding(top, 10);
        JLabel brand = new JLabel("CineBook");
        CineBookStyle.brand(brand);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.add(accountLabel);
        right.add(signOutBtn);
        signOutBtn.setVisible(false);
        top.add(brand, BorderLayout.WEST);
        top.add(right, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        tabs.addTab("Account", buildAccountTab());
        tabs.addTab("Movies", buildMoviesTab());
        tabs.addTab("Book Tickets", buildBookingTab());
        tabs.addTab("My Bookings", buildHistoryTab());
        tabs.addTab("Admin", buildAdminTab());
        tabs.setEnabledAt(4, false);
        add(tabs, BorderLayout.CENTER);
        add(new JLabel("Learning project - Core Java + Swing", SwingConstants.CENTER), BorderLayout.SOUTH);

        CineBookStyle.tabs(tabs);
        CineBookStyle.primary(loginBtn);
        CineBookStyle.primary(regBtn);
        CineBookStyle.primary(confirmBtn);
        CineBookStyle.primary(addMovieBtn);
        CineBookStyle.secondary(signOutBtn);
        CineBookStyle.secondary(refreshBtn);
        CineBookStyle.secondary(cancelBtn);
        CineBookStyle.secondary(deleteMovieBtn);
        CineBookStyle.table(movieTable);
        CineBookStyle.table(bookingTable);
        CineBookStyle.table(adminTable);
        for (int i = 0; i < 32; i++) CineBookStyle.seat(seatBtns[i], false, false);

        controller = new CineBookController(this);
        loginBtn.addActionListener(controller);
        loginPassword.addActionListener(controller);
        regBtn.addActionListener(controller);
        signOutBtn.addActionListener(controller);
        movieCombo.addActionListener(controller);
        theatreCombo.addActionListener(controller);
        timeCombo.addActionListener(controller);
        confirmBtn.addActionListener(controller);
        refreshBtn.addActionListener(controller);
        cancelBtn.addActionListener(controller);
        addMovieBtn.addActionListener(controller);
        deleteMovieBtn.addActionListener(controller);
        for (int i = 0; i < 32; i++) seatBtns[i].addActionListener(controller);
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { controller.refreshMovies(); }
        });
        controller.start();
    }

    static DefaultTableModel makeModel(String[] columns) {
        return new DefaultTableModel(columns, 0) {
            private static final long serialVersionUID = 1L;
            public boolean isCellEditable(int row, int col) { return false; }
        };
    }

    private JPanel buildForm(String title, String[] labels, JComponent[] fields, JButton button, JLabel msg) {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(BorderFactory.createTitledBorder(title));
        JPanel grid = new JPanel(new GridLayout(labels.length, 2, 8, 8));
        for (int i = 0; i < labels.length; i++) {
            grid.add(new JLabel(labels[i]));
            grid.add(fields[i]);
        }
        JPanel south = new JPanel(new BorderLayout(4, 4));
        south.add(button, BorderLayout.NORTH);
        south.add(msg, BorderLayout.CENTER);
        p.add(grid, BorderLayout.NORTH);
        p.add(south, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildAccountTab() {
        JPanel tab = new JPanel(new GridLayout(1, 2, 16, 16));
        CineBookStyle.padding(tab, 16);
        tab.add(buildForm("Sign in", new String[]{"Email", "Password"},
                new JComponent[]{loginEmail, loginPassword}, loginBtn, loginMsg));
        tab.add(buildForm("Create account", new String[]{"Your name", "Email", "Password (8+ chars)"},
                new JComponent[]{regName, regEmail, regPassword}, regBtn, regMsg));
        return tab;
    }

    private JPanel buildMoviesTab() {
        JPanel tab = new JPanel(new BorderLayout(8, 8));
        CineBookStyle.padding(tab, 12);
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchRow.add(new JLabel("Search movie titles:"));
        searchRow.add(searchField);
        tab.add(searchRow, BorderLayout.NORTH);
        tab.add(new JScrollPane(movieTable), BorderLayout.CENTER);
        return tab;
    }

    private JPanel buildBookingTab() {
        JPanel tab = new JPanel(new BorderLayout(8, 8));
        CineBookStyle.padding(tab, 12);

        JPanel choices = new JPanel(new GridLayout(3, 2, 8, 8));
        choices.add(new JLabel("Movie"));      choices.add(movieCombo);
        choices.add(new JLabel("Theatre"));    choices.add(theatreCombo);
        choices.add(new JLabel("Show time"));  choices.add(timeCombo);
        tab.add(choices, BorderLayout.NORTH);

        JPanel seatArea = new JPanel(new BorderLayout(6, 6));
        JLabel screen = new JLabel("SCREEN", SwingConstants.CENTER);
        CineBookStyle.screen(screen);
        seatArea.add(screen, BorderLayout.NORTH);
        JPanel grid = new JPanel(new GridLayout(4, 8, 6, 6));
        for (int i = 0; i < 32; i++) {
            String name = "" + "ABCD".charAt(i / 8) + ((i % 8) + 1);
            seatBtns[i] = new JToggleButton(name);
            seatGroup.add(seatBtns[i]);
            grid.add(seatBtns[i]);
        }
        seatArea.add(grid, BorderLayout.CENTER);
        tab.add(seatArea, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new GridLayout(3, 1, 4, 4));
        bottom.add(new JLabel("Green = available   Red = booked   Gold = selected", SwingConstants.CENTER));
        bottom.add(confirmBtn);
        bottom.add(bookingMsg);
        tab.add(bottom, BorderLayout.SOUTH);
        return tab;
    }

    private JPanel buildHistoryTab() {
        JPanel tab = new JPanel(new BorderLayout(8, 8));
        CineBookStyle.padding(tab, 12);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(refreshBtn);
        buttons.add(cancelBtn);
        tab.add(buttons, BorderLayout.NORTH);
        tab.add(new JScrollPane(bookingTable), BorderLayout.CENTER);
        return tab;
    }

    private JPanel buildAdminTab() {
        JPanel tab = new JPanel(new BorderLayout(8, 8));
        CineBookStyle.padding(tab, 12);
        tab.add(buildForm("Add a movie", new String[]{"Title", "Genre", "Duration (minutes)", "Price (Rs.)"},
                new JComponent[]{newTitle, newGenre, newDuration, newPrice}, addMovieBtn, adminMsg),
                BorderLayout.NORTH);
        JPanel list = new JPanel(new BorderLayout(4, 4));
        list.setBorder(BorderFactory.createTitledBorder("Current movies"));
        list.add(new JScrollPane(adminTable), BorderLayout.CENTER);
        list.add(deleteMovieBtn, BorderLayout.SOUTH);
        tab.add(list, BorderLayout.CENTER);
        return tab;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new CineBookGUI().setVisible(true);
            }
        });
    }
}
