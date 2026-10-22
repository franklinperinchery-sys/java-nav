import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class Database implements Bookable {
    private HashMap<String, Person> users = new HashMap<String, Person>();
    private ArrayList<Movie> movies = new ArrayList<Movie>();
    private ArrayList<Booking> bookings = new ArrayList<Booking>();
    private int movieCounter = 1, bookingCounter = 1;

    public void initialize() {
        if (!users.containsKey("admin@cinebook.com"))
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
        for (String[] m : sample) {
            boolean exists = false;
            for (Movie x : movies)
                if (x.getTitle().equalsIgnoreCase(m[0])) exists = true;
            if (!exists) addMovie(m[0], m[1], Integer.parseInt(m[2]), Double.parseDouble(m[3]));
        }
    }

    public Person findUserByEmail(String email) {
        return users.get(email.trim().toLowerCase());
    }

    public Person findUserById(int id) {
        for (Person p : users.values())
            if (p.id == id) return p;
        return null;
    }

    public int registerUser(String name, String email, String password) throws DuplicateEmailException {
        String key = email.trim().toLowerCase();
        if (users.containsKey(key))
            throw new DuplicateEmailException("An account with that email already exists.");
        Person p = new Customer(name, key, password);
        users.put(key, p);
        return p.id;
    }

    public ArrayList<Movie> movies(String search) {
        ArrayList<Movie> rows = new ArrayList<Movie>();
        String text = search == null ? "" : search.trim().toLowerCase();
        for (Movie m : movies)
            if (m.getTitle().toLowerCase().contains(text)) rows.add(m);
        Collections.sort(rows, new Comparator<Movie>() {
            public int compare(Movie a, Movie b) {
                return a.getTitle().compareToIgnoreCase(b.getTitle());
            }
        });
        return rows;
    }

    public Movie findMovie(int id) {
        for (Movie m : movies)
            if (m.getId() == id) return m;
        return null;
    }

    public int addMovie(String title, String genre, int duration, double price) {
        movies.add(new Movie(movieCounter, title, genre, duration, price));
        return movieCounter++;
    }

    public boolean updateMovie(int id, String title, String genre, int duration, double price) {
        Movie m = findMovie(id);
        if (m == null) return false;
        m.setTitle(title); m.setGenre(genre); m.setDuration(duration); m.setPrice(price);
        return true;
    }

    public boolean deleteMovie(int id) {
        Movie m = findMovie(id);
        if (m == null) return false;
        for (Booking b : bookings)
            if (b.movieId == id)
                throw new IllegalStateException("This movie has bookings.");
        return movies.remove(m);
    }

    public ArrayList<Booking> bookings(int userId, boolean admin) {
        ArrayList<Booking> rows = new ArrayList<Booking>();
        for (int i = bookings.size() - 1; i >= 0; i--) {
            Booking b = bookings.get(i);
            if (admin || b.userId == userId) rows.add(b);
        }
        return rows;
    }

    public ArrayList<String> bookedSeats(int movieId, String showTime, String theatre) {
        ArrayList<String> seats = new ArrayList<String>();
        for (Booking b : bookings)
            if (b.movieId == movieId && b.showTime.equals(showTime)
                    && b.theatre.equals(theatre) && b.status.equals("BOOKED"))
                seats.add(b.seat);
        return seats;
    }

    public int createBooking(int userId, int movieId, String showTime, String theatre, String seat)
            throws SeatUnavailableException {
        Person u = findUserById(userId);
        Movie m = findMovie(movieId);
        if (u == null || m == null) throw new IllegalArgumentException("Movie not found.");
        if (bookedSeats(movieId, showTime, theatre).contains(seat))
            throw new SeatUnavailableException("That seat is already booked for this show.");
        Booking b = new Booking(bookingCounter++, u, m, showTime, theatre, seat);
        bookings.add(b);
        return b.id;
    }

    public boolean cancelBooking(int id, int userId, boolean admin) {
        for (Booking b : bookings) {
            if (b.id == id && b.status.equals("BOOKED") && (admin || b.userId == userId)) {
                b.status = "CANCELLED";
                return true;
            }
        }
        return false;
    }
}

interface Bookable {
    int createBooking(int userId, int movieId, String showTime, String theatre, String seat)
            throws SeatUnavailableException;
    boolean cancelBooking(int id, int userId, boolean admin);
}

class SeatUnavailableException extends Exception {
    private static final long serialVersionUID = 1L;
    SeatUnavailableException(String message) { super(message); }
}

class DuplicateEmailException extends Exception {
    private static final long serialVersionUID = 1L;
    DuplicateEmailException(String message) { super(message); }
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
    void setTitle(String t) { title = t; }
    void setGenre(String g) { genre = g; }
    void setDuration(int d) { duration = d; }
    void setPrice(double p) { price = p; }
}

class Booking {
    int id, userId, movieId;
    String customerName, email, movieTitle, showTime, theatre, seat, status;

    Booking(int id, Person u, Movie m, String showTime, String theatre, String seat) {
        this.id = id; this.userId = u.id; this.customerName = u.name; this.email = u.email;
        this.movieId = m.getId(); this.movieTitle = m.getTitle();
        this.showTime = showTime; this.theatre = theatre; this.seat = seat;
        this.status = "BOOKED";
    }
}
