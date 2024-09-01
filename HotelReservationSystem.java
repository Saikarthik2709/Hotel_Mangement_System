import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// Model Classes
class Room {
    private int roomId;
    private String category;
    private double pricePerNight;
    private boolean isAvailable;

    public Room(int roomId, String category, double pricePerNight, boolean isAvailable) {
        this.roomId = roomId;
        this.category = category;
        this.pricePerNight = pricePerNight;
        this.isAvailable = isAvailable;
    }

    public int getRoomId() {
        return roomId;
    }

    public String getCategory() {
        return category;
    }

    public double getPricePerNight() {
        return pricePerNight;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }
}

class Reservation {
    private int reservationId;
    private Room room;
    private String guestName;
    private Date checkInDate;
    private Date checkOutDate;
    private double totalAmount;

    public Reservation(int reservationId, Room room, String guestName, Date checkInDate, Date checkOutDate, double totalAmount) {
        this.reservationId = reservationId;
        this.room = room;
        this.guestName = guestName;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.totalAmount = totalAmount;
    }

    public int getReservationId() {
        return reservationId;
    }

    public String getGuestName() {
        return guestName;
    }

    public double getTotalAmount() {
        return totalAmount;
    }
}

// Service Classes
class RoomService {
    private List<Room> rooms;

    public RoomService(List<Room> rooms) {
        this.rooms = rooms;
    }

    public List<Room> searchAvailableRooms(String category) {
        return rooms.stream()
                .filter(room -> room.isAvailable() && room.getCategory().equalsIgnoreCase(category))
                .collect(java.util.stream.Collectors.toList());
    }

    public void updateRoomAvailability(int roomId, boolean isAvailable) {
        rooms.stream()
                .filter(room -> room.getRoomId() == roomId)
                .forEach(room -> room.setAvailable(isAvailable));
    }
}

class ReservationService {
    private List<Reservation> reservations = new ArrayList<>();
    private RoomService roomService;

    public ReservationService(RoomService roomService) {
        this.roomService = roomService;
    }

    public Reservation makeReservation(int roomId, String guestName, Date checkInDate, Date checkOutDate) {
        Room room = roomService.searchAvailableRooms(null)
                .stream()
                .filter(r -> r.getRoomId() == roomId)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Room not available"));

        double totalAmount = (checkOutDate.getTime() - checkInDate.getTime()) / (1000 * 60 * 60 * 24) * room.getPricePerNight();
        Reservation reservation = new Reservation(reservations.size() + 1, room, guestName, checkInDate, checkOutDate, totalAmount);
        reservations.add(reservation);
        roomService.updateRoomAvailability(roomId, false);
        return reservation;
    }

    public List<Reservation> getReservations() {
        return reservations;
    }
}

// Main Class with GUI
public class HotelReservationSystem {
    private static JTextArea outputArea;

    public static void main(String[] args) {
        // Create the frame
        JFrame frame = new JFrame("Hotel Reservation System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLayout(new BorderLayout());

        // Create the text area for output
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        // Create buttons
        JPanel buttonPanel = new JPanel();
        JButton searchButton = new JButton("Search Available Rooms");
        JButton reserveButton = new JButton("Make a Reservation");
        JButton viewButton = new JButton("View Reservations");
        JButton exitButton = new JButton("Exit");
        buttonPanel.add(searchButton);
        buttonPanel.add(reserveButton);
        buttonPanel.add(viewButton);
        buttonPanel.add(exitButton);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        // Sample data
        List<Room> rooms = new ArrayList<>();
        rooms.add(new Room(101, "Single", 100.0, true));
        rooms.add(new Room(102, "Double", 150.0, true));
        rooms.add(new Room(103, "Suite", 200.0, true));

        RoomService roomService = new RoomService(rooms);
        ReservationService reservationService = new ReservationService(roomService);

        // Button actions
        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String category = JOptionPane.showInputDialog("Enter room category (Single/Double/Suite): ");
                List<Room> availableRooms = roomService.searchAvailableRooms(category);
                outputArea.setText("");
                availableRooms.forEach(room -> outputArea.append("Room ID: " + room.getRoomId() + ", Price: " + room.getPricePerNight() + "\n"));
            }
        });

        reserveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    int roomId = Integer.parseInt(JOptionPane.showInputDialog("Enter room ID: "));
                    String guestName = JOptionPane.showInputDialog("Enter guest name: ");
                    String checkIn = JOptionPane.showInputDialog("Enter check-in date (dd-MM-yyyy): ");
                    String checkOut = JOptionPane.showInputDialog("Enter check-out date (dd-MM-yyyy): ");

                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                    Date checkInDate = dateFormat.parse(checkIn);
                    Date checkOutDate = dateFormat.parse(checkOut);

                    Reservation reservation = reservationService.makeReservation(roomId, guestName, checkInDate, checkOutDate);
                    outputArea.setText("Reservation successful. Reservation ID: " + reservation.getReservationId());
                } catch (Exception ex) {
                    outputArea.setText("Error making reservation: " + ex.getMessage());
                }
            }
        });

        viewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                outputArea.setText("");
                List<Reservation> reservations = reservationService.getReservations();
                reservations.forEach(reservation -> outputArea.append("Reservation ID: " + reservation.getReservationId() + ", Guest: " + reservation.getGuestName() + ", Total: " + reservation.getTotalAmount() + "\n"));
            }
        });

        exitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
            }
        });

        // Display the frame
        frame.setVisible(true);
    }
}
