import javax.swing.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class CineBookController implements ActionListener {
    private CineBookGUI gui;
    Cinema cinema = new Cinema();
    Person currentUser = null;
    String selectedSeat = null;
    boolean updating = false;
    ArrayList<Movie> comboMovies = new ArrayList<Movie>();
    ArrayList<Booking> shownBookings = new ArrayList<Booking>();
    ArrayList<Movie> adminMovies = new ArrayList<Movie>();

    CineBookController(CineBookGUI gui) {
        this.gui = gui;
    }

    void start() {
        refreshMovies();
        loadBookings();
    }

    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (updating) return;
        if (src == gui.loginBtn || src == gui.loginPassword) doLogin();
        else if (src == gui.regBtn) doRegister();
        else if (src == gui.signOutBtn) doSignOut();
        else if (src == gui.movieCombo || src == gui.theatreCombo || src == gui.timeCombo) loadSeats();
        else if (src == gui.confirmBtn) doBook();
        else if (src == gui.refreshBtn) loadBookings();
        else if (src == gui.cancelBtn) doCancel();
        else if (src == gui.addMovieBtn) doAddMovie();
        else if (src == gui.deleteMovieBtn) doDeleteMovie();
        else if (src instanceof JToggleButton) seatClicked((JToggleButton) src);
    }

    void doLogin() {
        Person p = cinema.login(gui.loginEmail.getText(), new String(gui.loginPassword.getPassword()));
        if (p == null) {
            CineBookStyle.message(gui.loginMsg, "Incorrect email or password.", false);
            return;
        }
        currentUser = p;
        gui.loginEmail.setText("");
        gui.loginPassword.setText("");
        CineBookStyle.message(gui.loginMsg, "Signed in successfully.", true);
        updateAccount();
        loadBookings();
        gui.tabs.setSelectedIndex(1);
    }

    void doRegister() {
        String err = cinema.register(gui.regName.getText(), gui.regEmail.getText(),
                new String(gui.regPassword.getPassword()));
        if (err != null) {
            CineBookStyle.message(gui.regMsg, err, false);
            return;
        }
        gui.regName.setText("");
        gui.regEmail.setText("");
        gui.regPassword.setText("");
        CineBookStyle.message(gui.regMsg, "Account created. Sign in using your email and password.", true);
    }

    void doSignOut() {
        currentUser = null;
        updateAccount();
        loadBookings();
        CineBookStyle.message(gui.bookingMsg, "You signed out.", true);
        gui.tabs.setSelectedIndex(0);
    }

    void updateAccount() {
        boolean admin = currentUser instanceof Admin;
        if (currentUser == null) gui.accountLabel.setText("Not signed in");
        else gui.accountLabel.setText("Hi, " + currentUser.name + " (" + currentUser.getRole() + ")");
        gui.signOutBtn.setVisible(currentUser != null);
        gui.tabs.setEnabledAt(4, admin);
        if (admin) loadAdminMovies();
    }

    void refreshMovies() {
        ArrayList<Movie> found = cinema.searchMovies(gui.searchField.getText());
        gui.movieModel.setRowCount(0);
        for (Movie m : found)
            gui.movieModel.addRow(new Object[]{m.getTitle(), m.getGenre(), m.getDuration(), m.getPrice()});

        updating = true;
        int previousId = -1;
        int idx = gui.movieCombo.getSelectedIndex();
        if (idx >= 0 && idx < comboMovies.size()) previousId = comboMovies.get(idx).getId();
        gui.movieCombo.removeAllItems();
        comboMovies = found;
        int select = 0;
        for (int i = 0; i < found.size(); i++) {
            gui.movieCombo.addItem(found.get(i).getTitle());
            if (found.get(i).getId() == previousId) select = i;
        }
        if (!found.isEmpty()) gui.movieCombo.setSelectedIndex(select);
        updating = false;

        loadSeats();
        if (currentUser instanceof Admin) loadAdminMovies();
    }

    void loadSeats() {
        selectedSeat = null;
        gui.seatGroup.clearSelection();
        int idx = gui.movieCombo.getSelectedIndex();
        ArrayList<String> booked = new ArrayList<String>();
        if (idx >= 0 && idx < comboMovies.size())
            booked = cinema.bookedSeats(comboMovies.get(idx).getId(),
                    (String) gui.timeCombo.getSelectedItem(), (String) gui.theatreCombo.getSelectedItem());
        for (int i = 0; i < 32; i++) {
            boolean isBooked = booked.contains(gui.seatBtns[i].getText());
            gui.seatBtns[i].setEnabled(!isBooked && idx >= 0);
            gui.seatBtns[i].setToolTipText(isBooked ? "Already booked" : "Available seat");
            CineBookStyle.seat(gui.seatBtns[i], isBooked, false);
        }
    }

    void seatClicked(JToggleButton clicked) {
        selectedSeat = clicked.isSelected() ? clicked.getText() : null;
        for (int i = 0; i < 32; i++)
            if (gui.seatBtns[i].isEnabled())
                CineBookStyle.seat(gui.seatBtns[i], false, gui.seatBtns[i].isSelected());
    }

    void doBook() {
        if (currentUser == null) {
            CineBookStyle.message(gui.bookingMsg, "Please sign in before booking.", false);
            gui.tabs.setSelectedIndex(0);
            return;
        }
        int idx = gui.movieCombo.getSelectedIndex();
        if (idx < 0) {
            CineBookStyle.message(gui.bookingMsg, "Choose a movie first.", false);
            return;
        }
        if (selectedSeat == null) {
            CineBookStyle.message(gui.bookingMsg, "Please select an available seat.", false);
            return;
        }
        try {
            String seat = selectedSeat;
            cinema.book(currentUser, comboMovies.get(idx).getId(),
                    (String) gui.timeCombo.getSelectedItem(), (String) gui.theatreCombo.getSelectedItem(), seat);
            CineBookStyle.message(gui.bookingMsg, "Success! Seat " + seat + " is booked.", true);
        } catch (SeatUnavailableException ex) {
            CineBookStyle.message(gui.bookingMsg, ex.getMessage(), false);
        } catch (IllegalArgumentException ex) {
            CineBookStyle.message(gui.bookingMsg, ex.getMessage(), false);
        }
        loadSeats();
        loadBookings();
    }

    void loadBookings() {
        gui.bookingModel.setRowCount(0);
        shownBookings = new ArrayList<Booking>();
        if (currentUser == null) return;
        shownBookings = cinema.bookingsFor(currentUser);
        for (Booking b : shownBookings)
            gui.bookingModel.addRow(new Object[]{b.id, b.customerName, b.movieTitle, b.theatre,
                    b.showTime, b.seat, b.status});
    }

    void doCancel() {
        if (currentUser == null) {
            JOptionPane.showMessageDialog(gui, "Please sign in.");
            return;
        }
        int row = gui.bookingTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(gui, "Select a booking in the table first.");
            return;
        }
        Booking b = shownBookings.get(row);
        int ok = JOptionPane.showConfirmDialog(gui, "Cancel booking #" + b.id + "?",
                "Confirm", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        if (cinema.cancel(b.id, currentUser)) {
            loadBookings();
            loadSeats();
        } else {
            JOptionPane.showMessageDialog(gui,
                    "Active booking not found or you do not have permission to cancel it.");
        }
    }

    void loadAdminMovies() {
        adminMovies = cinema.searchMovies("");
        gui.adminModel.setRowCount(0);
        for (Movie m : adminMovies)
            gui.adminModel.addRow(new Object[]{m.getId(), m.getTitle(), m.getGenre(), m.getPrice()});
    }

    void doAddMovie() {
        if (!(currentUser instanceof Admin)) {
            CineBookStyle.message(gui.adminMsg, "Admin access required.", false);
            return;
        }
        try {
            int duration = Integer.parseInt(gui.newDuration.getText().trim());
            double price = Double.parseDouble(gui.newPrice.getText().trim());
            String err = cinema.validateMovie(gui.newTitle.getText(), gui.newGenre.getText(), duration, price);
            if (err != null) {
                CineBookStyle.message(gui.adminMsg, err, false);
                return;
            }
            cinema.addMovie(gui.newTitle.getText(), gui.newGenre.getText(), duration, price);
            gui.newTitle.setText("");
            gui.newGenre.setText("");
            gui.newDuration.setText("120");
            gui.newPrice.setText("200");
            CineBookStyle.message(gui.adminMsg, "Movie added.", true);
            refreshMovies();
        } catch (NumberFormatException ex) {
            CineBookStyle.message(gui.adminMsg, "Duration and price must be numbers.", false);
        }
    }

    void doDeleteMovie() {
        if (!(currentUser instanceof Admin)) return;
        int row = gui.adminTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(gui, "Select a movie in the list first.");
            return;
        }
        Movie m = adminMovies.get(row);
        int ok = JOptionPane.showConfirmDialog(gui,
                "Delete " + m.getTitle() + "? Movies with booking history cannot be deleted.",
                "Confirm", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        if (cinema.deleteMovie(m.getId())) refreshMovies();
        else JOptionPane.showMessageDialog(gui,
                "This movie may have bookings. Keep it rather than deleting its history.");
    }
}

class SeatUnavailableException extends Exception {
    private static final long serialVersionUID = 1L;
    public SeatUnavailableException(String message) {
        super(message);
    }
}

abstract class Person {
    private static int counter = 1;
    protected int id;
    protected String name, email, password;

    Person(String name, String email, String password) {
        this.id = counter++;
        this.name = name;
        this.email = email.toLowerCase();
        this.password = password;
    }
    abstract String getRole();
    boolean checkPassword(String p) { return password.equals(p); }
}

class Customer extends Person {
    Customer(String n, String e, String p) { super(n, e, p); }
    String getRole() { return "USER"; }
}

class Admin extends Person {
    Admin(String n, String e, String p) { super(n, e, p); }
    String getRole() { return "ADMIN"; }
}

class Movie {
    private int id, duration;
    private String title, genre;
    private double price;

    Movie(int id, String title, String genre, int duration, double price) {
        this.id = id; this.title = title; this.genre = genre;
        this.duration = duration; this.price = price;
    }
    int getId() { return id; }
    String getTitle() { return title; }
    String getGenre() { return genre; }
    int getDuration() { return duration; }
    double getPrice() { return price; }
}

class Booking {
    int id, userId, movieId;
    String customerName, movieTitle, showTime, theatre, seat, status;

    Booking(int id, Person p, Movie m, String showTime, String theatre, String seat) {
        this.id = id; this.userId = p.id; this.customerName = p.name;
        this.movieId = m.getId(); this.movieTitle = m.getTitle();
        this.showTime = showTime; this.theatre = theatre; this.seat = seat;
        this.status = "BOOKED";
    }
}

interface Bookable {
    Booking book(Person p, int movieId, String showTime, String theatre, String seat)
            throws SeatUnavailableException;
    boolean cancel(int bookingId, Person p);
}

class Cinema implements Bookable {
    private ArrayList<Movie> movies = new ArrayList<Movie>();
    private ArrayList<Booking> bookings = new ArrayList<Booking>();
    private HashMap<String, Person> users = new HashMap<String, Person>();
    private int movieCounter = 1, bookingCounter = 1;

    Cinema() {
        users.put("admin@cinebook.com", new Admin("Administrator", "admin@cinebook.com", "admin123"));
        String[][] sample = {
            {"Udumbanchola Vision", "Malayalam - Action, Drama", "135", "180"},
            {"Chiram", "Malayalam - Drama, Romance", "116", "170"},
            {"Randu Rahasyangal", "Malayalam - Crime, Thriller", "121", "180"},
            {"Poru: Oru Tharam, Randu Tharam", "Malayalam - Comedy, Drama", "139", "170"},
            {"Dhoomakethu", "Malayalam - Drama", "128", "180"},
            {"Aaram", "Malayalam - Thriller", "132", "180"},
            {"Lokah Chapter 1: Chandra", "Malayalam - Fantasy, Action", "151", "220"},
            {"L2: Empuraan", "Malayalam - Action, Thriller", "179", "250"},
            {"Thudarum", "Malayalam - Drama, Thriller", "163", "220"},
            {"Jailer 2", "Tamil - Action, Drama", "163", "230"},
            {"Drones", "Tamil - Thriller", "128", "180"},
            {"Titanic", "Tamil - Drama, Romance", "135", "180"},
            {"Dorothy", "Tamil - Drama", "142", "190"},
            {"Good Bad Ugly", "Tamil - Action, Comedy", "138", "220"},
            {"Dragon", "Tamil - Comedy, Drama", "157", "200"},
            {"Nayyi Navelli", "Hindi - Drama", "130", "180"},
            {"Prahaar", "Hindi - Action, Drama", "140", "190"},
            {"Pehla Pyaar Doosri Baar", "Hindi - Romance, Comedy - Upcoming", "125", "180"},
            {"Dhurandhar", "Hindi - Action, Thriller", "155", "230"},
            {"Saiyaara", "Hindi - Romance, Drama", "156", "200"},
            {"Ranabaali", "Telugu - Action, Drama", "170", "220"},
            {"Garividi Lakshmi", "Telugu - Drama, Biography", "122", "180"},
            {"Crazy Kalyanam", "Telugu - Comedy, Drama", "144", "190"},
            {"VISA - Vintara Saradaga", "Telugu - Comedy, Drama", "132", "180"},
            {"Aadarsha Kutumbam House No. 47", "Telugu - Family, Drama - Upcoming", "142", "200"},
            {"Interstellar", "English - Sci-Fi", "169", "250"},
            {"Inception", "English - Thriller", "148", "220"},
            {"The Dark Knight", "English - Action", "152", "240"},
            {"Inside Out 2", "English - Animation", "96", "180"}
        };
        for (String[] m : sample)
            addMovie(m[0], m[1], Integer.parseInt(m[2]), Double.parseDouble(m[3]));
    }

    String register(String name, String email, String password) {
        if (name.trim().isEmpty() || !email.trim().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
                || password.length() < 8)
            return "Enter your name, a valid email, and a password of at least 8 characters.";
        if (users.containsKey(email.trim().toLowerCase()))
            return "An account with that email already exists.";
        Person p = new Customer(name.trim(), email.trim(), password);
        users.put(p.email, p);
        return null;
    }

    Person login(String email, String password) {
        Person p = users.get(email.trim().toLowerCase());
        if (p != null && p.checkPassword(password)) return p;
        return null;
    }

    String validateMovie(String title, String genre, int duration, double price) {
        if (title.trim().isEmpty() || genre.trim().isEmpty() || duration <= 0 || price < 0)
            return "Enter a title, genre, positive duration and non-negative price.";
        return null;
    }

    int addMovie(String title, String genre, int duration, double price) {
        movies.add(new Movie(movieCounter, title.trim(), genre.trim(), duration, price));
        return movieCounter++;
    }

    Movie findMovie(int id) {
        for (Movie m : movies)
            if (m.getId() == id) return m;
        return null;
    }

    ArrayList<Movie> searchMovies(String text) {
        ArrayList<Movie> result = new ArrayList<Movie>();
        for (Movie m : movies)
            if (m.getTitle().toLowerCase().contains(text.trim().toLowerCase())) result.add(m);
        Collections.sort(result, new Comparator<Movie>() {
            public int compare(Movie a, Movie b) {
                return a.getTitle().compareToIgnoreCase(b.getTitle());
            }
        });
        return result;
    }

    boolean deleteMovie(int id) {
        for (Booking b : bookings)
            if (b.movieId == id) return false;
        Movie m = findMovie(id);
        return m != null && movies.remove(m);
    }

    ArrayList<String> bookedSeats(int movieId, String showTime, String theatre) {
        ArrayList<String> seats = new ArrayList<String>();
        for (Booking b : bookings)
            if (b.movieId == movieId && b.showTime.equals(showTime)
                    && b.theatre.equals(theatre) && b.status.equals("BOOKED"))
                seats.add(b.seat);
        return seats;
    }

    static boolean validSeat(String s) {
        return s != null && s.matches("[A-D][1-8]");
    }

    public Booking book(Person p, int movieId, String showTime, String theatre, String seat)
            throws SeatUnavailableException {
        Movie m = findMovie(movieId);
        if (m == null) throw new IllegalArgumentException("Movie not found.");
        if (!validSeat(seat)) throw new IllegalArgumentException("Choose a valid seat (A1-D8).");
        if (bookedSeats(movieId, showTime, theatre).contains(seat))
            throw new SeatUnavailableException("That seat is already booked for this show.");
        Booking b = new Booking(bookingCounter++, p, m, showTime, theatre, seat);
        bookings.add(b);
        return b;
    }

    public boolean cancel(int bookingId, Person p) {
        for (Booking b : bookings) {
            if (b.id == bookingId && b.status.equals("BOOKED")
                    && (p instanceof Admin || b.userId == p.id)) {
                b.status = "CANCELLED";
                return true;
            }
        }
        return false;
    }

    ArrayList<Booking> bookingsFor(Person p) {
        ArrayList<Booking> result = new ArrayList<Booking>();
        for (int i = bookings.size() - 1; i >= 0; i--) {
            Booking b = bookings.get(i);
            if (p instanceof Admin || b.userId == p.id) result.add(b);
        }
        return result;
    }
}
