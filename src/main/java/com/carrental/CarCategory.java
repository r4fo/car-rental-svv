package com.carrental;

/**
 * The three car categories the company rents out (FR1).
 * Each category has a fixed daily rate in US dollars.
 */
public enum CarCategory {
    ECONOMY(40.0),
    STANDARD(60.0),
    LUXURY(120.0);

    private final double dailyRate;

    CarCategory(double dailyRate) {
        this.dailyRate = dailyRate;
    }

    public double getDailyRate() {
        return dailyRate;
    }
}
