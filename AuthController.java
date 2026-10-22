public class AuthController {
    private final Database db;

    public AuthController(Database db) { this.db = db; }

    public Response register(String name, String email, String password) {
        if (name == null || name.trim().isEmpty() || email == null
                || !email.trim().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
                || password == null || password.length() < 8)
            return new Response(400, "Enter your name, a valid email, and a password of at least 8 characters.");
        if (db.findUserByEmail(email) != null)
            return new Response(409, "An account with that email already exists.");
        try {
            db.registerUser(name.trim(), email.trim(), password);
            return new Response(201, "Account created. Please sign in.");
        } catch (DuplicateEmailException e) {
            return new Response(409, e.getMessage());
        }
    }

    public Response login(String email, String password, Session session) {
        if (email == null || password == null)
            return new Response(400, "Enter email and password.");
        Person user = db.findUserByEmail(email);
        if (user == null || !user.checkPassword(password))
            return new Response(401, "Incorrect email or password.");
        session.user = user;
        return new Response(200, "Welcome, " + user.name + " (" + user.getRole() + ")");
    }

    public Response logout(Session session) {
        session.user = null;
        return new Response(200, "Signed out.");
    }

    public Response me(Session session) {
        if (!session.isLoggedIn())
            return new Response(401, "Please sign in.");
        Person u = session.user;
        return new Response(200, u.id + " | " + u.name + " | " + u.email + " | " + u.getRole());
    }
}
