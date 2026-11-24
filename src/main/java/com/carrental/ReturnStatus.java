package com.carrental;

/**
 * The status of the car when it comes back (FR15).
 */
public enum ReturnStatus {
    /** Returned within the 60-minute grace period. */
    ON_TIME,
    /** Returned 1 or 2 days late: late fees only. */
    LATE,
    /** Returned 3 or more days late: late fees and the case is referred to legal action. */
    LEGAL_ACTION
}
