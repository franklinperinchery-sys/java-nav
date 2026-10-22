import java.util.ArrayList;

public class MovieController {
    private final Database db;

    public MovieController(Database db) { this.db = db; }

    public void showMovies(String search) {
        ArrayList<Movie> list = db.movies(search);
        if (list.isEmpty()) System.out.println("No movies found.");
        for (Movie m : list)
            System.out.println(String.format("%-3d %-32s %-36s %4d min  Rs.%.2f",
                    m.getId(), m.getTitle(), m.getGenre(), m.getDuration(), m.getPrice()));
    }

    public Response addMovie(Session s, String title, String genre, int duration, double price) {
        Response denied = adminOnly(s);
        if (denied != null) return denied;
        Response invalid = validateMovie(title, genre, duration, price);
        if (invalid != null) return invalid;
        int id = db.addMovie(title.trim(), genre.trim(), duration, price);
        return new Response(201, "Movie added with id " + id + ".");
    }

    public Response updateMovie(Session s, int id, String title, String genre, int duration, double price) {
        Response denied = adminOnly(s);
        if (denied != null) return denied;
        Response invalid = validateMovie(title, genre, duration, price);
        if (invalid != null) return invalid;
        if (!db.updateMovie(id, title.trim(), genre.trim(), duration, price))
            return new Response(404, "Movie not found.");
        return new Response(200, "Movie updated.");
    }

    public Response deleteMovie(Session s, int id) {
        Response denied = adminOnly(s);
        if (denied != null) return denied;
        try {
            if (db.deleteMovie(id)) return new Response(200, "Movie deleted.");
            return new Response(404, "Movie not found.");
        } catch (IllegalStateException e) {
            return new Response(409, "This movie may have bookings. Keep it rather than deleting its history.");
        }
    }

    public void showSeats(int movieId, String showTime, String theatre) {
        ArrayList<String> booked = db.bookedSeats(movieId, showTime, theatre);
        System.out.println("Seat map ([XX] = booked)");
        for (char r = 'A'; r <= 'D'; r++) {
            for (int c = 1; c <= 8; c++) {
                String seat = "" + r + c;
                System.out.print(booked.contains(seat) ? "[XX] " : "[" + seat + "] ");
            }
            System.out.println();
        }
    }

    public Response showBookings(Session s) {
        if (!s.isLoggedIn())
            return new Response(401, "Please sign in to view bookings.");
        ArrayList<Booking> list = db.bookings(s.user.id, s.isAdmin());
        if (list.isEmpty()) System.out.println("No bookings found.");
        for (Booking b : list)
            System.out.println("#" + b.id + " " + b.movieTitle + " | " + b.showTime + " | " + b.theatre
                    + " | Seat " + b.seat + " | " + b.status + (s.isAdmin() ? " | " + b.customerName : ""));
        return new Response(200, "");
    }

    public Response book(Session s, int movieId, String showTime, String theatre, String seat) {
        if (!s.isLoggedIn())
            return new Response(401, "Please sign in before booking.");
        if (movieId <= 0 || showTime == null || showTime.trim().isEmpty()
                || theatre == null || theatre.trim().isEmpty()
                || seat == null || !seat.matches("[A-D][1-8]"))
            return new Response(400, "Choose a movie, theatre, show time and valid seat (A1-D8).");
        try {
            int id = db.createBooking(s.user.id, movieId, showTime, theatre, seat);
            return new Response(201, "Ticket booked. Booking id = " + id);
        } catch (SeatUnavailableException e) {
            return new Response(409, e.getMessage());
        } catch (IllegalArgumentException e) {
            return new Response(404, e.getMessage());
        }
    }

    public Response cancel(Session s, int id) {
        if (!s.isLoggedIn())
            return new Response(401, "Please sign in.");
        if (db.cancelBooking(id, s.user.id, s.isAdmin()))
            return new Response(200, "Booking cancelled.");
        return new Response(404, "Active booking not found or you do not have permission to cancel it.");
    }

    private Response adminOnly(Session s) {
        if (!s.isLoggedIn()) return new Response(401, "Please sign in.");
        if (!s.isAdmin()) return new Response(403, "Admin access required.");
        return null;
    }

    private Response validateMovie(String title, String genre, int duration, double price) {
        if (title == null || title.trim().isEmpty() || genre == null || genre.trim().isEmpty()
                || duration <= 0 || price < 0)
            return new Response(400, "Enter a title, genre, positive duration and non-negative price.");
        return null;
    }
}
