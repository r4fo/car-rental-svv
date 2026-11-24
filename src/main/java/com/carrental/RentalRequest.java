package com.carrental;

/**
 * All the information about one rental, collected when the car is returned.
 *
 * The constructor checks every input (FR6, FR11). If an input is invalid it throws
 * IllegalArgumentException, so an invalid RentalRequest object can never exist.
 */
public class RentalRequest {

    public static final int MIN_DAYS = 1;
    public static final int MAX_DAYS = 30;
    public static final int MIN_LICENCE_AGE = 18;

    private final int age;
    private final int licenceYears;
    private final CarCategory category;
    private final int days;
    private final int kmDriven;
    private final int lateMinutes;

    public RentalRequest(int age, int licenceYears, CarCategory category,
                         int days, int kmDriven, int lateMinutes) {
        if (age < 0) {
            throw new IllegalArgumentException("Age cannot be negative");
        }
        if (licenceYears < 0) {
            throw new IllegalArgumentException("Licence years cannot be negative");
        }
        // Nobody can hold a licence before age 18, so licence years cannot be
        // more than (age - 18). Math.max keeps the limit at 0 for people under 18.
        if (licenceYears > Math.max(0, age - MIN_LICENCE_AGE)) {
            throw new IllegalArgumentException("Licence years are not possible for this age");
        }
        if (category == null) {
            throw new IllegalArgumentException("Car category is required");
        }
        if (days < MIN_DAYS || days > MAX_DAYS) {
            throw new IllegalArgumentException("Rental days must be between 1 and 30");
        }
        if (kmDriven < 0) {
            throw new IllegalArgumentException("Kilometres driven cannot be negative");
        }
        if (lateMinutes < 0) {
            throw new IllegalArgumentException("Late minutes cannot be negative");
        }

        this.age = age;
        this.licenceYears = licenceYears;
        this.category = category;
        this.days = days;
        this.kmDriven = kmDriven;
        this.lateMinutes = lateMinutes;
    }

    public int getAge() {
        return age;
    }

    public int getLicenceYears() {
        return licenceYears;
    }

    public CarCategory getCategory() {
        return category;
    }

    public int getDays() {
        return days;
    }

    public int getKmDriven() {
        return kmDriven;
    }

    public int getLateMinutes() {
        return lateMinutes;
    }
}
