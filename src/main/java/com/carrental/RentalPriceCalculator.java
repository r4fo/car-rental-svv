package com.carrental;

/**
 * Calculates every part of the rental bill (FR5, FR7-FR10, FR12-FR15).
 *
 * Each part of the price has its own small method. This keeps the code readable
 * and lets each rule be tested on its own.
 */
public class RentalPriceCalculator {

    public static final int DISCOUNT_MIN_DAYS = 7;
    public static final double DISCOUNT_RATE = 0.10;
    public static final double YOUNG_DRIVER_FEE_PER_DAY = 10.0;
    public static final int FREE_KM_PER_DAY = 200;
    public static final double EXTRA_KM_FEE = 0.20;
    public static final int GRACE_MINUTES = 60;
    public static final int MINUTES_PER_DAY = 1440;
    public static final double LATE_FEE_PER_DAY = 25.0;
    public static final int LEGAL_ACTION_DAYS = 3;
    public static final double DEPOSIT = 300.0;

    private final EligibilityChecker eligibilityChecker;

    public RentalPriceCalculator(EligibilityChecker eligibilityChecker) {
        this.eligibilityChecker = eligibilityChecker;
    }

    /**
     * Rounds a dollar amount to whole cents.
     * Fix for defect D2: double arithmetic gave 3 x 0.20 = 0.6000000000000001.
     */
    private static double roundToCents(double amount) {
        return Math.round(amount * 100) / 100.0;
    }

    /** Days multiplied by the daily rate of the category. */
    public double basePrice(RentalRequest request) {
        return roundToCents(request.getDays() * request.getCategory().getDailyRate());
    }

    /** FR7: 10% of the base price when the rental is 7 days or longer, otherwise 0. */
    public double discount(RentalRequest request) {
        if (request.getDays() >= DISCOUNT_MIN_DAYS) {
            return roundToCents(basePrice(request) * DISCOUNT_RATE);
        }
        return 0.0;
    }

    /** FR5: $10 per day, only for drivers who were approved with a surcharge. */
    public double youngDriverFee(RentalRequest request) {
        if (eligibilityChecker.check(request) == Eligibility.APPROVED_WITH_SURCHARGE) {
            return roundToCents(request.getDays() * YOUNG_DRIVER_FEE_PER_DAY);
        }
        return 0.0;
    }

    /** FR8: 200 km per day are included, every extra km costs $0.20. */
    public double mileageFee(RentalRequest request) {
        int includedKm = request.getDays() * FREE_KM_PER_DAY;
        int extraKm = Math.max(0, request.getKmDriven() - includedKm);
        return roundToCents(extraKm * EXTRA_KM_FEE);
    }

    /**
     * FR9: number of late days to charge.
     * Up to 60 minutes late is free. After that, every started 24 hours counts as one day,
     * so 61 minutes = 1 day, 1440 minutes = 1 day and 1441 minutes = 2 days.
     */
    public int lateDays(RentalRequest request) {
        int minutes = request.getLateMinutes();
        if (minutes <= GRACE_MINUTES) {
            return 0;
        }
        // "Ceiling division": rounds up to the next whole day.
        // Fix for defect D1: the sum is done in long, because with int the value
        // overflowed for very large inputs and gave a negative number of days.
        return (int) ((minutes + (long) MINUTES_PER_DAY - 1) / MINUTES_PER_DAY);
    }

    /** FR9: $25 for every late day. */
    public double lateFee(RentalRequest request) {
        return roundToCents(lateDays(request) * LATE_FEE_PER_DAY);
    }

    /**
     * FR13 (version 2): the part of the $300 deposit returned to the customer.
     * Late fees are taken from the deposit first, so the refund is never below 0.
     */
    public double depositRefund(RentalRequest request) {
        return roundToCents(Math.max(0.0, DEPOSIT - lateFee(request)));
    }

    /**
     * FR14 (version 2): what the customer still has to pay when the late fees
     * are larger than the deposit. 0 when the deposit covers them.
     */
    public double amountOwedAfterDeposit(RentalRequest request) {
        return roundToCents(Math.max(0.0, lateFee(request) - DEPOSIT));
    }

    /** FR15: ON_TIME (0 late days), LATE (1-2 days) or LEGAL_ACTION (3+ days). */
    public ReturnStatus returnStatus(RentalRequest request) {
        int days = lateDays(request);
        if (days == 0) {
            return ReturnStatus.ON_TIME;
        }
        if (days < LEGAL_ACTION_DAYS) {
            return ReturnStatus.LATE;
        }
        return ReturnStatus.LEGAL_ACTION;
    }

    /**
     * FR10: the full bill.
     * FR12: asking for the price of a rejected customer is an incorrect operation,
     * so it throws IllegalStateException instead of returning a number.
     */
    public double totalPrice(RentalRequest request) {
        if (eligibilityChecker.check(request) == Eligibility.REJECTED) {
            throw new IllegalStateException("Cannot calculate a price for a rejected customer");
        }
        return roundToCents(basePrice(request)
                - discount(request)
                + youngDriverFee(request)
                + mileageFee(request)
                + lateFee(request));
    }
}
