package com.carrental;

/**
 * The possible answers to "may this customer rent this car?" (FR2, FR3, FR4).
 */
public enum Eligibility {
    /** The customer may rent the car at the normal price. */
    APPROVED,
    /** The customer may rent the car but pays the young driver fee (age 21-24). */
    APPROVED_WITH_SURCHARGE,
    /** The customer may not rent the car. */
    REJECTED
}
