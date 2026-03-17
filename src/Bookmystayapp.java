import java.util.HashMap;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Stack;
import java.util.*;
import java.util.concurrent.locks.*;
import java.io.*;
import java.util.*;

class PersistenceService {

    private static final String FILE_NAME = "hotel_state.ser";

    public static void saveState(RoomInventory inventory, BookingHistory history, RoomAllocationService allocator) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            out.writeObject(inventory);
            out.writeObject(history);
            out.writeObject(allocator.getAllocatedRoomIds());
            System.out.println("\nSystem state saved successfully.");
        } catch (IOException e) {
            System.out.println("Error saving system state: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public static boolean loadState(RoomInventory inventory, BookingHistory history, RoomAllocationService allocator) {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            System.out.println("No saved system state found. Starting fresh.");
            return false;
        }

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            RoomInventory savedInventory = (RoomInventory) in.readObject();
            BookingHistory savedHistory = (BookingHistory) in.readObject();
            Set<String> savedAllocatedRooms = (Set<String>) in.readObject();

            for (String roomType : savedInventory.getInventoryMap().keySet()) {
                inventory.updateAvailability(roomType, savedInventory.getAvailability(roomType));
            }

            for (Reservation r : savedHistory.getBookings()) {
                history.addBooking(r);
            }

            allocator.getAllocatedRoomIds().clear();
            allocator.getAllocatedRoomIds().addAll(savedAllocatedRooms);

            System.out.println("System state restored successfully.");
            return true;
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading system state: " + e.getMessage());
            return false;
        }
    }
}
class InvalidBookingException extends Exception {

    public InvalidBookingException(String message) {
        super(message);
    }
}
class CancellationService {

    private Stack<String> rollbackStack;
    private RoomInventory inventory;
    private Set<String> allocatedRoomIds;

    public CancellationService(RoomInventory inventory, Set<String> allocatedRoomIds) {
        this.inventory = inventory;
        this.allocatedRoomIds = allocatedRoomIds;
        rollbackStack = new Stack<>();
    }

    public void cancelBooking(String roomId, String roomType) {

        System.out.println("\nCancellation Request for Room ID: " + roomId);

        if (!allocatedRoomIds.contains(roomId)) {
            System.out.println("Cancellation Failed: Reservation does not exist.");
            return;
        }

        rollbackStack.push(roomId);

        allocatedRoomIds.remove(roomId);

        int current = inventory.getAvailability(roomType);
        inventory.updateAvailability(roomType, current + 1);

        System.out.println("Booking cancelled successfully.");
        System.out.println("Room ID released: " + roomId);
        System.out.println("Inventory restored for " + roomType);
    }

    public void displayRollbackHistory() {

        System.out.println("\nRollback Stack (Recently Cancelled Rooms):");

        if (rollbackStack.isEmpty()) {
            System.out.println("No cancellations recorded.");
            return;
        }

        for (String id : rollbackStack) {
            System.out.println(id);
        }
    }
}
class Reservation implements Serializable  {
    private static final long serialVersionUID = 1L;
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

class RoomInventory implements Serializable {
    private static final long serialVersionUID = 1L;
    private HashMap<String, Integer> inventory;
    public void displayInventory() {
        System.out.println("\n=== Current Room Inventory ===");
        for (String roomType : inventory.keySet()) {
            System.out.println(roomType + ": " + inventory.get(roomType) + " available");
        }
    }
    public RoomInventory() {
        inventory = new HashMap<>();
        inventory.put("Single Room", 10);
        inventory.put("Double Room", 6);
        inventory.put("Suite Room", 3);
    }
    public void decrementRoom(String roomType) {
        int current = inventory.getOrDefault(roomType, 0);
        if (current > 0) {
            inventory.put(roomType, current - 1);
        }
    }
    public int getAvailability(String roomType) {
        return inventory.getOrDefault(roomType, 0);
    }

    public void updateAvailability(String roomType, int count) {
        inventory.put(roomType, count);
    }

    public HashMap<String, Integer> getInventoryMap() {
        return inventory;
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

    protected RoomInventory inventory;
    private Set<String> allocatedRoomIds;
    private HashMap<String, Set<String>> roomTypeAllocations;

    public RoomAllocationService(RoomInventory inventory) {
        this.inventory = inventory;
        allocatedRoomIds = new HashSet<>();
        roomTypeAllocations = new HashMap<>();
    }

    protected String generateRoomId(String roomType) {

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
    public Set<String> getAllocatedRoomIds() {
        return allocatedRoomIds;
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
class BookingHistory  implements Serializable{
    private static final long serialVersionUID = 1L;
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
class ThreadSafeBookingQueue {
    private final Queue<Reservation> queue = new LinkedList<>();

    public synchronized void addRequest(Reservation r) {
        queue.add(r);
        System.out.println("Booking request added for " + r.getGuestName());
    }

    public synchronized Reservation getNextRequest() {
        return queue.poll();
    }

    public synchronized boolean isEmpty() {
        return queue.isEmpty();
    }
}
class ThreadSafeRoomAllocationService extends RoomAllocationService {

    private final Lock lock = new ReentrantLock();

    public ThreadSafeRoomAllocationService(RoomInventory inventory) {
        super(inventory);
    }

    public void processBooking(Reservation reservation) {
        lock.lock();
        try {
            InvalidBookingValidator.validateReservation(reservation, super.inventory);
            String roomType = reservation.getRoomType();
            String roomId = super.generateRoomId(roomType);
            super.getAllocatedRoomIds().add(roomId);
            super.inventory.decrementRoom(roomType);
            System.out.println("Reservation Confirmed! Guest: " + reservation.getGuestName() +
                    " | Room ID: " + roomId);
        } catch (InvalidBookingException e) {
            System.out.println("Booking Failed for " + reservation.getGuestName() + ": " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }
}

class BookingProcessorThread extends Thread {
    private ThreadSafeBookingQueue queue;
    private ThreadSafeRoomAllocationService allocator;

    public BookingProcessorThread(ThreadSafeBookingQueue queue, ThreadSafeRoomAllocationService allocator) {
        this.queue = queue;
        this.allocator = allocator;
    }

    @Override
    public void run() {
        while (true) {
            Reservation r;
            synchronized (queue) {
                if (queue.isEmpty()) break;
                r = queue.getNextRequest();
            }
            if (r != null) {
                allocator.processBooking(r);
            }
        }
    }
}
public class Bookmystayapp {

    public static void main(String[] args) {

        System.out.println("=====================================");
        System.out.println("     Welcome to Book My Stay App     ");
        System.out.println("     Hotel Booking System v1.0       ");
        System.out.println("=====================================");

        RoomInventory inventory = new RoomInventory();
        BookingHistory history = new BookingHistory();
        RoomAllocationService allocator = new RoomAllocationService(inventory);

        PersistenceService.loadState(inventory, history, allocator);

        inventory.displayInventory();
        Room[] rooms = {new SingleRoom(), new DoubleRoom(), new SuiteRoom()};
        RoomSearchService searchService = new RoomSearchService(inventory);
        searchService.searchRooms(rooms);

        BookingRequestQueue bookingQueue = new BookingRequestQueue();
        Reservation r1 = new Reservation("Michael Robinavitch", "Single Room");
        Reservation r2 = new Reservation("Trinity Santos", "Double Room");
        Reservation r3 = new Reservation("Samira Mohan", "Suite Room");

        bookingQueue.addRequest(r1);
        bookingQueue.addRequest(r2);
        bookingQueue.addRequest(r3);
        allocator.processBookings(bookingQueue);

        AddOnServiceManager serviceManager = new AddOnServiceManager();
        String reservationId = "SR101";
        serviceManager.addService(reservationId, new AddOnService("Breakfast", 500));
        serviceManager.addService(reservationId, new AddOnService("Airport Pickup", 1200));
        serviceManager.addService(reservationId, new AddOnService("Spa Access", 2000));
        serviceManager.displayServices(reservationId);

        history.addBooking(r1);
        history.addBooking(r2);
        history.addBooking(r3);
        history.displayHistory();

        BookingReportService reportService = new BookingReportService();
        reportService.generateReport(history.getBookings());

        Reservation r4 = new Reservation("", "Luxury Room");
        bookingQueue.addRequest(r4);
        allocator.processBookings(bookingQueue);

        CancellationService cancellationService =
                new CancellationService(inventory, allocator.getAllocatedRoomIds());
        if (!allocator.getAllocatedRoomIds().isEmpty()) {
            String cancelRoomId = allocator.getAllocatedRoomIds().iterator().next();
            cancellationService.cancelBooking(cancelRoomId, "Single Room");
        }
        cancellationService.displayRollbackHistory();
        inventory.displayInventory();

        System.out.println("\n=== Concurrent Booking Simulation ===");
        ThreadSafeBookingQueue concurrentQueue = new ThreadSafeBookingQueue();
        ThreadSafeRoomAllocationService threadSafeAllocator = new ThreadSafeRoomAllocationService(inventory);

        Reservation[] concurrentReservations = {
                new Reservation("Alice", "Single Room"),
                new Reservation("Bob", "Double Room"),
                new Reservation("Charlie", "Suite Room"),
                new Reservation("David", "Single Room"),
                new Reservation("Eve", "Double Room"),
                new Reservation("Frank", "Suite Room"),
                new Reservation("Grace", "Single Room"),
                new Reservation("Hannah", "Double Room")
        };

        for (Reservation r : concurrentReservations) {
            concurrentQueue.addRequest(r);
        }

        int threadCount = 3;
        Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new BookingProcessorThread(concurrentQueue, threadSafeAllocator);
            threads[i].start();
        }

        for (Thread t : threads) {
            try { t.join(); } catch (InterruptedException e) { e.printStackTrace(); }
        }

        System.out.println("\nAll concurrent booking requests processed.");
        System.out.println("Final Inventory:");
        inventory.displayInventory();
        System.out.println("Allocated Rooms: " + threadSafeAllocator.getAllocatedRoomIds().size());

        PersistenceService.saveState(inventory, history, allocator);

        System.out.println("\nApplication executed successfully.");
    }
}