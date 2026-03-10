import java.util.HashMap;
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
}
/**
 * Book My Stay Application
 *
 * This class represents the entry point of the Hotel Booking Management System.
 * It demonstrates how a Java program begins execution using the main() method
 * and prints a welcome message to the console.
 *
 * The application currently focuses on establishing the startup behavior
 * before additional booking features are implemented.
 *
 * @author YourName
 * @version 1.0
 */
public class Bookmystayapp {
    /**
     * Main method - entry point of the Java application.
     * The JVM invokes this method to start the program.
     *
     * @param args command-line arguments passed during execution
     */
    public static void main(String[] args) {

        // Display welcome message
        System.out.println("=====================================");
        System.out.println("     Welcome to Book My Stay App     ");
        System.out.println("     Hotel Booking System v1.0       ");
        System.out.println("=====================================");

        // Inform user that application started successfully
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

        System.out.println("Application executed successfully.");
    }
}

