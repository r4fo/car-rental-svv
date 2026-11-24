package com.carrental;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests the input validation in RentalRequest (FR6, FR11).
 */
@DisplayName("RentalRequest input validation")
class RentalRequestTest {

    @Nested
    @DisplayName("Equivalence partitioning")
    class EquivalencePartitioning {

        @Test
        @DisplayName("TC-01: a request where every input is in its valid class is created")
        void validRequestIsCreated() {
            RentalRequest request = new RentalRequest(30, 5, CarCategory.STANDARD, 5, 800, 0);

            assertEquals(30, request.getAge());
            assertEquals(5, request.getLicenceYears());
            assertEquals(CarCategory.STANDARD, request.getCategory());
            assertEquals(5, request.getDays());
            assertEquals(800, request.getKmDriven());
            assertEquals(0, request.getLateMinutes());
        }
    }

    @Nested
    @DisplayName("Boundary value analysis")
    class BoundaryValues {

        @ParameterizedTest(name = "TC-02: {0} days is accepted")
        @ValueSource(ints = {1, 2, 29, 30})
        void daysAtAndInsideBoundariesAreAccepted(int days) {
            assertDoesNotThrow(() -> new RentalRequest(30, 5, CarCategory.ECONOMY, days, 0, 0));
        }

        @ParameterizedTest(name = "TC-03: {0} days is rejected")
        @ValueSource(ints = {0, 31})
        void daysJustOutsideBoundariesAreRejected(int days) {
            assertThrows(IllegalArgumentException.class,
                    () -> new RentalRequest(30, 5, CarCategory.ECONOMY, days, 0, 0));
        }

        @ParameterizedTest(name = "TC-04: age {0} with {1} licence years is accepted")
        @CsvSource({"30, 12", "18, 0", "17, 0"})
        void licenceYearsAtTheirLimitAreAccepted(int age, int licenceYears) {
            assertDoesNotThrow(
                    () -> new RentalRequest(age, licenceYears, CarCategory.ECONOMY, 1, 0, 0));
        }

        @Test
        @DisplayName("TC-05: one licence year above (age - 18) is rejected")
        void licenceYearsAboveLimitAreRejected() {
            assertThrows(IllegalArgumentException.class,
                    () -> new RentalRequest(30, 13, CarCategory.ECONOMY, 1, 0, 0));
        }
    }

    @Nested
    @DisplayName("Negative testing")
    class NegativeTests {

        @Test
        @DisplayName("TC-06: negative age is rejected with a clear message")
        void negativeAge() {
            IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                    () -> new RentalRequest(-1, 0, CarCategory.ECONOMY, 1, 0, 0));
            assertEquals("Age cannot be negative", error.getMessage());
        }

        @Test
        @DisplayName("TC-07: negative licence years are rejected")
        void negativeLicenceYears() {
            assertThrows(IllegalArgumentException.class,
                    () -> new RentalRequest(30, -1, CarCategory.ECONOMY, 1, 0, 0));
        }

        @Test
        @DisplayName("TC-08: missing car category (null) is rejected")
        void nullCategory() {
            assertThrows(IllegalArgumentException.class,
                    () -> new RentalRequest(30, 5, null, 1, 0, 0));
        }

        @Test
        @DisplayName("TC-09: negative kilometres are rejected")
        void negativeKilometres() {
            assertThrows(IllegalArgumentException.class,
                    () -> new RentalRequest(30, 5, CarCategory.ECONOMY, 1, -1, 0));
        }

        @Test
        @DisplayName("TC-10: negative late minutes are rejected")
        void negativeLateMinutes() {
            assertThrows(IllegalArgumentException.class,
                    () -> new RentalRequest(30, 5, CarCategory.ECONOMY, 1, 0, -1));
        }
    }
}
