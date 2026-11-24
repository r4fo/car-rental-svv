package com.carrental;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests the price rules (FR5, FR7, FR8, FR9, FR10, FR12, FR15).
 */
@DisplayName("RentalPriceCalculator")
class RentalPriceCalculatorTest {

    /** Allowed rounding difference when comparing totals made of several fees. */
    private static final double DELTA = 0.001;

    private final RentalPriceCalculator calculator =
            new RentalPriceCalculator(new EligibilityChecker());

    /** An adult customer (no young driver fee) renting an economy car. */
    private RentalRequest adult(int days, int km, int lateMinutes) {
        return new RentalRequest(30, 5, CarCategory.ECONOMY, days, km, lateMinutes);
    }

    @Nested
    @DisplayName("Boundary value analysis")
    class Boundaries {

        @ParameterizedTest(name = "TC-16: {0} days -> discount ${1}")
        @CsvSource({
                "6, 0.0",
                "7, 28.0",
                "8, 32.0"
        })
        void discountStartsAtSevenDays(int days, double expectedDiscount) {
            assertEquals(expectedDiscount, calculator.discount(adult(days, 0, 0)), DELTA);
        }

        @ParameterizedTest(name = "TC-17: 1 day, {0} km -> mileage fee ${1}")
        @CsvSource({
                "199, 0.0",
                "200, 0.0",
                "201, 0.2"
        })
        void mileageFeeStartsAfterIncludedKilometres(int km, double expectedFee) {
            assertEquals(expectedFee, calculator.mileageFee(adult(1, km, 0)), DELTA);
        }

        @ParameterizedTest(name = "TC-18: {0} minutes late -> {1} late days, status {2}")
        @CsvSource({
                "0,    0, ON_TIME",
                "59,   0, ON_TIME",
                "60,   0, ON_TIME",
                "61,   1, LATE",
                "1440, 1, LATE",
                "1441, 2, LATE",
                "2880, 2, LATE",
                "2881, 3, LEGAL_ACTION",
                "4321, 4, LEGAL_ACTION"
        })
        void lateMinutesBoundaries(int lateMinutes, int expectedDays, ReturnStatus expectedStatus) {
            RentalRequest request = adult(3, 0, lateMinutes);

            assertEquals(expectedDays, calculator.lateDays(request));
            assertEquals(expectedDays * 25.0, calculator.lateFee(request), DELTA);
            assertEquals(expectedStatus, calculator.returnStatus(request));
        }

        @Test
        @DisplayName("TC-19: largest possible late time still gives a positive number of late days")
        void extremeLateMinutes() {
            // Integer.MAX_VALUE minutes = 1,491,308.09 days, so 1,491,309 started days.
            RentalRequest request = adult(3, 0, Integer.MAX_VALUE);

            assertEquals(1_491_309, calculator.lateDays(request));
            assertEquals(ReturnStatus.LEGAL_ACTION, calculator.returnStatus(request));
        }

        @Test
        @DisplayName("TC-20: fees are exact to the cent (3 extra km = $0.60)")
        void feesAreExactToTheCent() {
            // A bill is money: $0.60 must be exactly $0.60, so no DELTA is used here.
            RentalRequest request = adult(1, 203, 0);

            assertEquals(0.60, calculator.mileageFee(request));
        }
    }

    @Nested
    @DisplayName("Decision table: total price")
    class TotalPriceDecisionTable {

        @ParameterizedTest(name = "TC-21 {0}: age {1}, {2}, {3} days, {4} km, {5} min late -> ${6}")
        @CsvSource({
                // rule, age, licence, category, days, km, lateMin, expected total
                // T1: no discount, no young fee, no extra km, on time
                "T1, 30, 5, ECONOMY,  3, 600,  0,    120.0",
                // T2: discount + young fee + extra km + late (all rules active)
                "T2, 22, 2, STANDARD, 10, 2500, 1500, 790.0",
                // T3: discount + late, luxury, adult
                "T3, 40, 10, LUXURY,  7, 1400, 61,   781.0",
                // T4: young fee + extra km, no discount, within grace period
                "T4, 24, 3, ECONOMY,  6, 1205, 60,   301.0"
        })
        void totalPrice(String rule, int age, int licenceYears, CarCategory category,
                        int days, int km, int lateMinutes, double expectedTotal) {
            RentalRequest request =
                    new RentalRequest(age, licenceYears, category, days, km, lateMinutes);

            assertEquals(expectedTotal, calculator.totalPrice(request), DELTA, "Rule " + rule);
        }
    }

    @Nested
    @DisplayName("Version 2: $300 deposit (FR13, FR14)")
    class Deposit {

        @ParameterizedTest(name = "TC-24: {0} late days -> refund ${1}, still owed ${2}")
        @CsvSource({
                "0,  300.0, 0.0",
                "1,  275.0, 0.0",
                "11, 25.0,  0.0",
                "12, 0.0,   0.0",
                "13, 0.0,   25.0"
        })
        void depositBoundaries(int lateDays, double expectedRefund, double expectedOwed) {
            // lateDays * 1440 minutes is exactly that many days late.
            RentalRequest request = adult(3, 0, lateDays * 1440);

            assertEquals(lateDays, calculator.lateDays(request));
            assertEquals(expectedRefund, calculator.depositRefund(request), DELTA);
            assertEquals(expectedOwed, calculator.amountOwedAfterDeposit(request), DELTA);
        }

        @Test
        @DisplayName("TC-25: the deposit does not change the total price of the rental")
        void depositDoesNotChangeTotal() {
            RentalRequest request = new RentalRequest(22, 2, CarCategory.STANDARD, 10, 2500, 1500);

            assertEquals(790.0, calculator.totalPrice(request), DELTA);
        }
    }

    @Nested
    @DisplayName("Negative testing")
    class NegativeTests {

        @Test
        @DisplayName("TC-22: asking for the price of a rejected customer throws an exception")
        void priceForRejectedCustomer() {
            RentalRequest rejected = new RentalRequest(19, 1, CarCategory.ECONOMY, 3, 0, 0);

            IllegalStateException error = assertThrows(IllegalStateException.class,
                    () -> calculator.totalPrice(rejected));
            assertEquals("Cannot calculate a price for a rejected customer", error.getMessage());
        }

        @Test
        @DisplayName("TC-23: young driver fee is not charged to an adult")
        void noYoungDriverFeeForAdult() {
            assertEquals(0.0, calculator.youngDriverFee(adult(5, 0, 0)), DELTA);
        }
    }
}
