import javax.swing.*;
import java.awt.*;

class CineBookStyle {
    static final Color ACCENT = new Color(244, 189, 80);
    static final Color GREEN  = new Color(78, 214, 160);
    static final Color RED    = new Color(250, 115, 125);
    static final Color GOLD   = ACCENT;
    static final Color OK_TEXT  = new Color(20, 140, 90);
    static final Color ERR_TEXT = new Color(200, 40, 50);
    static final Color SCREEN_BG = new Color(190, 200, 220);
    static final Color BRAND_COLOR = new Color(200, 140, 20);
    static final Color DARK_TEXT = new Color(23, 19, 11);

    static final Font BRAND_FONT = new Font("SansSerif", Font.BOLD, 24);
    static final Font TAB_FONT   = new Font("SansSerif", Font.BOLD, 13);

    static void brand(JLabel label) {
        label.setFont(BRAND_FONT);
        label.setForeground(BRAND_COLOR);
    }

    static void primary(JButton b) {
        b.setBackground(ACCENT);
        b.setForeground(DARK_TEXT);
        b.setOpaque(true);
        b.setFocusPainted(false);
    }

    static void secondary(JButton b) {
        b.setFocusPainted(false);
    }

    static void screen(JLabel label) {
        label.setOpaque(true);
        label.setBackground(SCREEN_BG);
        label.setFont(TAB_FONT);
    }

    static void table(JTable t) {
        t.setRowHeight(24);
        t.getTableHeader().setFont(TAB_FONT);
        t.setSelectionBackground(ACCENT);
        t.setSelectionForeground(DARK_TEXT);
    }

    static void tabs(JTabbedPane tabs) {
        tabs.setFont(TAB_FONT);
    }

    static void padding(JComponent c, int px) {
        c.setBorder(BorderFactory.createEmptyBorder(px, px, px, px));
    }

    static void message(JLabel label, String text, boolean success) {
        label.setText(text);
        label.setForeground(success ? OK_TEXT : ERR_TEXT);
    }

    static void seat(JToggleButton b, boolean booked, boolean selected) {
        b.setOpaque(true);
        b.setBackground(booked ? RED : (selected ? GOLD : GREEN));
    }
}
