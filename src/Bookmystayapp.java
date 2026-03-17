import java.util.HashMap;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
class InvalidBookingException extends Exception {

    public InvalidBookingException(String message) {
        super(message);
    }
}
class Reservation {

    private String guestName;
    private String roomType;

    public Reservation(String guestName, String roomType) {
        this.guestName = guestName;
        this.roomType = roomType;
    }

    public String getGuestName() {
        return guestName;
    }

    public String getRoomType() {
        return roomType;
    }

    public void displayReservation() {
        System.out.println("Guest: " + guestName + " | Requested Room: " + roomType);
    }
}

class BookingRequestQueue {

    private Queue<Reservation> requestQueue;

    public BookingRequestQueue() {
        requestQueue = new LinkedList<>();
    }

    public void addRequest(Reservation reservation) {
        requestQueue.add(reservation);
        System.out.println("Booking request added for " + reservation.getGuestName());
    }

    public void displayQueue() {
        System.out.println("\nCurrent Booking Request Queue:");

        if (requestQueue.isEmpty()) {
            System.out.println("No booking requests.");
            return;
        }

        for (Reservation r : requestQueue) {
            r.displayReservation();
        }
    }
    public Reservation getNextRequest() {
        return requestQueue.poll();
    }

    public boolean isEmpty() {
        return requestQueue.isEmpty();
    }
}
abstract class Room {
    protected String roomType;
    protected int beds;
    protected int size;
    protected double price;

    public Room(String roomType, int beds, int size, double price) {
        this.roomType = roomType;
        this.beds = beds;
        this.size = size;
        this.price = price;
    }

    public String getRoomType() {
        return roomType;
    }

    public void displayRoomDetails() {
        System.out.println("Room Type  : " + roomType);
        System.out.println("Beds       : " + beds);
        System.out.println("Room Size  : " + size + " sq.ft");
        System.out.println("Price/Night: ₹" + price);
    }
}

class SingleRoom extends Room {
    public SingleRoom() {
        super("Single Room", 1, 180, 2500);
    }
}

class DoubleRoom extends Room {
    public DoubleRoom() {
        super("Double Room", 2, 250, 4000);
    }
}

class SuiteRoom extends Room {
    public SuiteRoom() {
        super("Suite Room", 3, 400, 7500);
    }
}

class RoomInventory {

    private HashMap<String, Integer> inventory;

    public RoomInventory() {
        inventory = new HashMap<>();
        inventory.put("Single Room", 10);
        inventory.put("Double Room", 6);
        inventory.put("Suite Room", 3);
    }

    public int getAvailability(String roomType) {
        return inventory.getOrDefault(roomType, 0);
    }

    public void updateAvailability(String roomType, int count) {
        inventory.put(roomType, count);
    }

    public void displayInventory() {
        System.out.println("Current Room Inventory:");
        for (String roomType : inventory.keySet()) {
            System.out.println(roomType + " : " + inventory.get(roomType));
        }
    }
    public void decrementRoom(String roomType) {
        int available = inventory.get(roomType);
        inventory.put(roomType, available - 1);
    }
}

class RoomSearchService {

    private RoomInventory inventory;

    public RoomSearchService(RoomInventory inventory) {
        this.inventory = inventory;
    }

    public void searchRooms(Room[] rooms) {
        System.out.println("\nAvailable Rooms:");

        for (Room room : rooms) {
            int available = inventory.getAvailability(room.getRoomType());

            if (available > 0) {
                room.displayRoomDetails();
                System.out.println("Available: " + available);
                System.out.println();
            }
        }
    }
}
class RoomAllocationService {

    private RoomInventory inventory;
    private Set<String> allocatedRoomIds;
    private HashMap<String, Set<String>> roomTypeAllocations;

    public RoomAllocationService(RoomInventory inventory) {
        this.inventory = inventory;
        allocatedRoomIds = new HashSet<>();
        roomTypeAllocations = new HashMap<>();
    }

    private String generateRoomId(String roomType) {

        String prefix = "";

        if (roomType.equals("Single Room"))
            prefix = "SR";
        else if (roomType.equals("Double Room"))
            prefix = "DR";
        else if (roomType.equals("Suite Room"))
            prefix = "SU";

        String roomId;

        do {
            int num = (int)(Math.random() * 1000);
            roomId = prefix + num;
        } while (allocatedRoomIds.contains(roomId));

        return roomId;
    }

    public void processBookings(BookingRequestQueue queue) {

        System.out.println("\nProcessing Booking Requests...\n");

        while (!queue.isEmpty()) {

            Reservation reservation = queue.getNextRequest();

            try {
                InvalidBookingValidator.validateReservation(reservation, inventory);
                String roomType = reservation.getRoomType();
                String roomId = generateRoomId(roomType);
                allocatedRoomIds.add(roomId);
                roomTypeAllocations
                        .computeIfAbsent(roomType, k -> new HashSet<>())
                        .add(roomId);
                inventory.decrementRoom(roomType);
                System.out.println("Reservation Confirmed!");
                System.out.println("Guest: " + reservation.getGuestName());
                System.out.println("Room ID: " + roomId);
                System.out.println();
            } catch (InvalidBookingException e) {
                System.out.println("Booking Failed: " + e.getMessage());
                System.out.println("Request skipped.\n");
            }
        }
    }
}
class AddOnService {

    private String serviceName;
    private double price;

    public AddOnService(String serviceName, double price) {
        this.serviceName = serviceName;
        this.price = price;
    }

    public String getServiceName() {
        return serviceName;
    }

    public double getPrice() {
        return price;
    }

    public void displayService() {
        System.out.println(serviceName + " - ₹" + price);
    }
}
class AddOnServiceManager {

    private HashMap<String, List<AddOnService>> reservationServices;

    public AddOnServiceManager() {
        reservationServices = new HashMap<>();
    }

    public void addService(String reservationId, AddOnService service) {

        reservationServices
                .computeIfAbsent(reservationId, k -> new ArrayList<>())
                .add(service);

        System.out.println(service.getServiceName() + " added to reservation " + reservationId);
    }

    public double calculateTotalServiceCost(String reservationId) {

        double total = 0;

        List<AddOnService> services = reservationServices.get(reservationId);

        if (services != null) {
            for (AddOnService s : services) {
                total += s.getPrice();
            }
        }

        return total;
    }

    public void displayServices(String reservationId) {

        System.out.println("\nServices for Reservation " + reservationId);

        List<AddOnService> services = reservationServices.get(reservationId);

        if (services == null) {
            System.out.println("No services selected.");
            return;
        }

        for (AddOnService s : services) {
            s.displayService();
        }
    }
}
class BookingHistory {

    private List<Reservation> confirmedBookings;

    public BookingHistory() {
        confirmedBookings = new ArrayList<>();
    }

    public void addBooking(Reservation reservation) {
        confirmedBookings.add(reservation);
    }

    public List<Reservation> getBookings() {
        return confirmedBookings;
    }

    public void displayHistory() {

        System.out.println("\nBooking History:");

        if (confirmedBookings.isEmpty()) {
            System.out.println("No confirmed bookings.");
            return;
        }

        for (Reservation r : confirmedBookings) {
            r.displayReservation();
        }
    }
}
class BookingReportService {

    public void generateReport(List<Reservation> bookings) {

        System.out.println("\n=== Booking Summary Report ===");

        if (bookings.isEmpty()) {
            System.out.println("No booking data available.");
            return;
        }

        HashMap<String, Integer> roomTypeCount = new HashMap<>();

        for (Reservation r : bookings) {

            String roomType = r.getRoomType();

            roomTypeCount.put(
                    roomType,
                    roomTypeCount.getOrDefault(roomType, 0) + 1
            );
        }

        for (String type : roomTypeCount.keySet()) {
            System.out.println(type + " Bookings: " + roomTypeCount.get(type));
        }

        System.out.println("Total Bookings: " + bookings.size());
    }
}
class InvalidBookingValidator {

    public static void validateReservation(Reservation reservation, RoomInventory inventory)
            throws InvalidBookingException {

        String roomType = reservation.getRoomType();

        if (!roomType.equals("Single Room") &&
                !roomType.equals("Double Room") &&
                !roomType.equals("Suite Room")) {

            throw new InvalidBookingException("Invalid room type: " + roomType);
        }

        if (inventory.getAvailability(roomType) <= 0) {
            throw new InvalidBookingException("No rooms available for " + roomType);
        }

        if (reservation.getGuestName() == null || reservation.getGuestName().trim().isEmpty()) {
            throw new InvalidBookingException("Guest name cannot be empty");
        }
    }
}
public class Bookmystayapp {

    public static void main(String[] args) {

        System.out.println("=====================================");
        System.out.println("     Welcome to Book My Stay App     ");
        System.out.println("     Hotel Booking System v1.0       ");
        System.out.println("=====================================");

        System.out.println("Application started successfully.");

        int singleRoomAvailability = 10;
        int doubleRoomAvailability = 6;
        int suiteRoomAvailability = 3;

        Room single = new SingleRoom();
        Room doubleRoom = new DoubleRoom();
        Room suite = new SuiteRoom();

        System.out.println("----- Single Room Details -----");
        single.displayRoomDetails();
        System.out.println("Available Rooms: " + singleRoomAvailability);
        System.out.println();

        System.out.println("----- Double Room Details -----");
        doubleRoom.displayRoomDetails();
        System.out.println("Available Rooms: " + doubleRoomAvailability);
        System.out.println();

        System.out.println("----- Suite Room Details -----");
        suite.displayRoomDetails();
        System.out.println("Available Rooms: " + suiteRoomAvailability);
        System.out.println();

        RoomInventory inventory = new RoomInventory();

        inventory.displayInventory();

        System.out.println("\nChecking availability for Double Room:");
        System.out.println("Available: " + inventory.getAvailability("Double Room"));

        System.out.println("\nUpdating availability for Suite Room...");
        inventory.updateAvailability("Suite Room", 5);

        System.out.println("\nUpdated Inventory:");
        inventory.displayInventory();

        Room[] rooms = {
                new SingleRoom(),
                new DoubleRoom(),
                new SuiteRoom()
        };

        RoomSearchService searchService = new RoomSearchService(inventory);

        searchService.searchRooms(rooms);
        BookingRequestQueue bookingQueue = new BookingRequestQueue();

        Reservation r1 = new Reservation("Michael Robinavitch", "Single Room");
        Reservation r2 = new Reservation("Trinity Santos", "Double Room");
        Reservation r3 = new Reservation("Samira Mohan", "Suite Room");

        bookingQueue.addRequest(r1);
        bookingQueue.addRequest(r2);
        bookingQueue.addRequest(r3);

        bookingQueue.displayQueue();
        RoomAllocationService allocator = new RoomAllocationService(inventory);
        allocator.processBookings(bookingQueue);
        AddOnServiceManager serviceManager = new AddOnServiceManager();

        AddOnService breakfast = new AddOnService("Breakfast", 500);
        AddOnService airportPickup = new AddOnService("Airport Pickup", 1200);
        AddOnService spa = new AddOnService("Spa Access", 2000);

        String reservationId = "SR101";

        serviceManager.addService(reservationId, breakfast);
        serviceManager.addService(reservationId, airportPickup);
        serviceManager.addService(reservationId, spa);

        serviceManager.displayServices(reservationId);

        double totalServiceCost = serviceManager.calculateTotalServiceCost(reservationId);

        System.out.println("Total Add-On Cost: ₹" + totalServiceCost);
        BookingHistory history = new BookingHistory();

        history.addBooking(r1);
        history.addBooking(r2);
        history.addBooking(r3);

        history.displayHistory();

        BookingReportService reportService = new BookingReportService();

        reportService.generateReport(history.getBookings());
        reportService.generateReport(history.getBookings());
        Reservation r4 = new Reservation("", "Luxury Room");
        bookingQueue.addRequest(r4);
        System.out.println("\nProcessing Invalid Booking Test:");
        allocator.processBookings(bookingQueue);
        System.out.println("Application executed successfully.");
    }
}