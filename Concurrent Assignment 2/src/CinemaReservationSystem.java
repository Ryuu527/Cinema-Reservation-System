import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

// Class representing the cinema reservation system
public class CinemaReservationSystem {
    // Array of Theatre objects representing different theatres in the cinema
    private static final Theatre[] theatres = {
            new Theatre(1, 20), // Theatre 1 with 20 seats
            new Theatre(2, 20), // Theatre 2 with 20 seats
            new Theatre(3, 20)  // Theatre 3 with 20 seats
    };

    public static void main(String[] args) {
        // Create a thread pool with 10 threads
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        // List to hold Future objects representing the result of reservation attempts
        List<Future<Boolean>> futures = new ArrayList<>();

        // Random number generator
        Random random = new Random();
        for (int i = 1; i <= 100; i++) {
            // Create a customer with a random selection of seats and theatre
            Customer customer = new Customer(
                    i,
                    random.nextInt(3) + 1,
                    generateRandomSeats(random)
            );

            // Submit the customer's reservation task to the executor service
            Future<Boolean> future = executorService.submit(() -> {
                return reserveSeats(customer);
            });
            // Add the Future object to the list
            futures.add(future);
        }

        // Wait for all reservation attempts to complete
        for (Future<Boolean> future : futures) {
            try {
                future.get(); // Retrieve the result of each reservation attempt
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace(); // Print stack trace in case of an exception
            }
        }

        executorService.shutdown(); // Shutdown the executor service
    }

    // Method to generate a random list of seats
    private static List<Integer> generateRandomSeats(Random random) {
        List<Integer> seats = new ArrayList<>();
        int numSeats = random.nextInt(3) + 1; // Randomly select the number of seats (1 to 3)
        for (int i = 0; i < numSeats; i++) {
            seats.add(random.nextInt(20) + 1); // Randomly select seat numbers (1 to 20)
        }
        return seats;
    }

    // Method to reserve seats for a customer
    private static boolean reserveSeats(Customer customer) {
        for (Theatre theatre : theatres) {
            if (theatre.getTheatreId() == customer.getTheatreId()) {
                return theatre.reserveSeats(customer);
            }
        }
        return false;
    }
}

// Class representing a theatre
class Theatre {
    private final int theatreId;
    private final int totalSeats;
    private final Set<Integer> reservedSeats = new HashSet<>();
    private final ReentrantLock lock = new ReentrantLock(); // Lock to ensure thread safety

    public Theatre(int theatreId, int totalSeats) {
        this.theatreId = theatreId;
        this.totalSeats = totalSeats;
    }

    public int getTheatreId() {
        return theatreId;
    }

    // Method to reserve seats for a customer
    public boolean reserveSeats(Customer customer) {
        lock.lock(); // Acquire the lock
        try {
            // Check if the requested seats are available
            if (reservedSeats.containsAll(customer.getSeats())) {
                System.out.println("Customer " + customer.getCustomerId() + " failed to reserve seats " + customer.getSeats() + " in theatre " + theatreId);
                return false;
            }

            // Simulate a delay before confirming the reservation
            try {
                Thread.sleep(new Random().nextInt(501) + 500); // Random delay between 500ms to 1000ms
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore interrupted state
            }

            // Reserve the seats
            reservedSeats.addAll(customer.getSeats());
            System.out.println("Customer " + customer.getCustomerId() + " confirmed reservation for seats " + customer.getSeats() + " in theatre " + theatreId);
            return true;
        } finally {
            lock.unlock(); // Release the lock
        }
    }
}

// Class representing a customer
class Customer {
    private final int customerId;
    private final int theatreId;
    private final List<Integer> seats;

    public Customer(int customerId, int theatreId, List<Integer> seats) {
        this.customerId = customerId;
        this.theatreId = theatreId;
        this.seats = seats;
    }

    public int getCustomerId() {
        return customerId;
    }

    public int getTheatreId() {
        return theatreId;
    }

    public List<Integer> getSeats() {
        return seats;
    }
}