package com.carrental;

/**
 * Decides whether a customer may rent the requested car (FR2, FR3, FR4).
 *
 * The rules are checked in order. The first rule that fails returns REJECTED,
 * so the order of the checks matters for the decision table.
 */
public class EligibilityChecker {

    public static final int MIN_AGE = 21;
    public static final int YOUNG_DRIVER_MAX_AGE = 24;
    public static final int MAX_AGE = 75;
    public static final int MIN_LICENCE_YEARS = 1;
    public static final int LUXURY_MIN_AGE = 25;
    public static final int LUXURY_MIN_LICENCE_YEARS = 3;

    public Eligibility check(RentalRequest request) {
        int age = request.getAge();
        int licenceYears = request.getLicenceYears();

        // FR2: age must be between 21 and 75 (both included).
        if (age < MIN_AGE || age > MAX_AGE) {
            return Eligibility.REJECTED;
        }

        // FR3: every customer needs at least 1 year of driving experience.
        if (licenceYears < MIN_LICENCE_YEARS) {
            return Eligibility.REJECTED;
        }

        // FR4: luxury cars need age 25+ AND at least 3 licence years.
        if (request.getCategory() == CarCategory.LUXURY
                && (age < LUXURY_MIN_AGE || licenceYears < LUXURY_MIN_LICENCE_YEARS)) {
            return Eligibility.REJECTED;
        }

        // FR2 / FR5: drivers aged 21-24 are accepted but pay a daily surcharge.
        if (age <= YOUNG_DRIVER_MAX_AGE) {
            return Eligibility.APPROVED_WITH_SURCHARGE;
        }

        return Eligibility.APPROVED;
    }
}
