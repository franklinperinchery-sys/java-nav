import java.util.Scanner;

public class MovieBookingApplication {
    static Scanner sc = new Scanner(System.in);
    static final String[] SHOW_TIMES = {"Today 6:00 PM", "Today 9:00 PM", "Tomorrow 3:00 PM"};
    static final String[] THEATRES = {"PVR Lulu, Kochi", "Cinepolis Centre Square, Kochi",
        "Dhanya Remya, Thrissur", "Kairali Sree Theatre, Alappuzha", "Crown Theatre, Kozhikode"};

    public static void main(String[] args) {
        Database db = new Database();
        db.initialize();
        AuthController auth = new AuthController(db);
        MovieController movies = new MovieController(db);
        Session session = new Session();

        System.out.println("===== CineBook - Movie Ticket Booking =====");
        System.out.println("(Default admin: admin@cinebook.com / admin123)");
        boolean running = true;
        while (running) {
            if (!session.isLoggedIn()) {
                System.out.println("\n1. Register  2. Login  3. View/Search movies  0. Exit");
                switch (readInt("Choice: ")) {
                    case 1:
                        print(auth.register(readLine("Name: "), readLine("Email: "), readLine("Password (min 8): ")));
                        break;
                    case 2:
                        print(auth.login(readLine("Email: "), readLine("Password: "), session));
                        break;
                    case 3:
                        movies.showMovies(readLine("Search title (blank = all): "));
                        break;
                    case 0:
                        running = false;
                        break;
                    default:
                        System.out.println("Invalid choice.");
                }
            } else {
                System.out.println("\n1. Movies  2. Book ticket  3. My bookings  4. Cancel booking  5. Who am I");
                if (session.isAdmin()) System.out.println("6. Add movie  7. Update movie  8. Delete movie");
                System.out.println("0. Logout");
                switch (readInt("Choice: ")) {
                    case 1:
                        movies.showMovies(readLine("Search title (blank = all): "));
                        break;
                    case 2:
                        movies.showMovies("");
                        int movieId = readInt("Movie id: ");
                        String theatre = choose("Theatre:", THEATRES);
                        String time = choose("Show time:", SHOW_TIMES);
                        movies.showSeats(movieId, time, theatre);
                        String seat = readLine("Seat (A1-D8): ").trim().toUpperCase();
                        print(movies.book(session, movieId, time, theatre, seat));
                        break;
                    case 3:
                        print(movies.showBookings(session));
                        break;
                    case 4:
                        movies.showBookings(session);
                        print(movies.cancel(session, readInt("Booking id to cancel: ")));
                        break;
                    case 5:
                        print(auth.me(session));
                        break;
                    case 6:
                        print(movies.addMovie(session, readLine("Title: "), readLine("Genre: "),
                                readInt("Duration (min): "), readDouble("Price: ")));
                        break;
                    case 7:
                        print(movies.updateMovie(session, readInt("Movie id: "), readLine("New title: "),
                                readLine("New genre: "), readInt("New duration: "), readDouble("New price: ")));
                        break;
                    case 8:
                        print(movies.deleteMovie(session, readInt("Movie id: ")));
                        break;
                    case 0:
                        print(auth.logout(session));
                        break;
                    default:
                        System.out.println("Invalid choice.");
                }
            }
        }
        System.out.println("Thank you!");
    }

    static void print(Response r) {
        if (!r.message.isEmpty()) System.out.println((r.isOk() ? "" : "Error: ") + r.message);
    }

    static String readLine(String prompt) {
        System.out.print(prompt);
        return sc.nextLine();
    }

    static int readInt(String prompt) {
        while (true) {
            try {
                return Integer.parseInt(readLine(prompt).trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number.");
            }
        }
    }

    static double readDouble(String prompt) {
        while (true) {
            try {
                return Double.parseDouble(readLine(prompt).trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid amount.");
            }
        }
    }

    static String choose(String title, String[] options) {
        System.out.println(title);
        for (int i = 0; i < options.length; i++)
            System.out.println("  " + (i + 1) + ". " + options[i]);
        int n;
        do {
            n = readInt("Choice: ");
        } while (n < 1 || n > options.length);
        return options[n - 1];
    }
}

class Session {
    Person user = null;
    boolean isLoggedIn() { return user != null; }
    boolean isAdmin() { return user instanceof Admin; }
}

class Response {
    final int status;
    final String message;
    Response(int status, String message) { this.status = status; this.message = message; }
    boolean isOk() { return status < 400; }
}
