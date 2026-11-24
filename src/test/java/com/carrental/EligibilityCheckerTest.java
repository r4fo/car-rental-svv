package com.carrental;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the eligibility rules (FR2, FR3, FR4).
 */
@DisplayName("EligibilityChecker")
class EligibilityCheckerTest {

    private final EligibilityChecker checker = new EligibilityChecker();

    private Eligibility check(int age, int licenceYears, CarCategory category) {
        return checker.check(new RentalRequest(age, licenceYears, category, 3, 0, 0));
    }

    @Nested
    @DisplayName("Equivalence partitioning: age classes")
    class AgeClasses {

        @ParameterizedTest(name = "TC-11: age {0} -> {1}")
        @CsvSource({
                "18, REJECTED",                  // class 1: under 21
                "23, APPROVED_WITH_SURCHARGE",   // class 2: young driver 21-24
                "40, APPROVED",                  // class 3: normal 25-75
                "80, REJECTED"                   // class 4: over 75
        })
        void oneValuePerAgeClass(int age, Eligibility expected) {
            // licence years = 0 is impossible to use here for age 18, so use the
            // largest valid licence for each age (capped at 5).
            int licenceYears = Math.min(5, Math.max(1, age - 18));
            if (age < 19) {
                licenceYears = 0;
            }
            assertEquals(expected, check(age, licenceYears, CarCategory.ECONOMY));
        }
    }

    @Nested
    @DisplayName("Boundary value analysis: age and licence limits")
    class Boundaries {

        @ParameterizedTest(name = "TC-12: age {0} -> {1}")
        @CsvSource({
                "20, REJECTED",
                "21, APPROVED_WITH_SURCHARGE",
                "22, APPROVED_WITH_SURCHARGE",
                "24, APPROVED_WITH_SURCHARGE",
                "25, APPROVED",
                "26, APPROVED",
                "74, APPROVED",
                "75, APPROVED",
                "76, REJECTED"
        })
        void ageBoundaries(int age, Eligibility expected) {
            assertEquals(expected, check(age, 2, CarCategory.ECONOMY));
        }

        @ParameterizedTest(name = "TC-13: {0} licence years -> {1}")
        @CsvSource({
                "0, REJECTED",
                "1, APPROVED",
                "2, APPROVED"
        })
        void licenceBoundaries(int licenceYears, Eligibility expected) {
            assertEquals(expected, check(30, licenceYears, CarCategory.ECONOMY));
        }

        @ParameterizedTest(name = "TC-14: luxury car, {0} licence years -> {1}")
        @CsvSource({
                "2, REJECTED",
                "3, APPROVED",
                "4, APPROVED"
        })
        void luxuryLicenceBoundaries(int licenceYears, Eligibility expected) {
            assertEquals(expected, check(30, licenceYears, CarCategory.LUXURY));
        }
    }

    @Nested
    @DisplayName("Decision table: age x licence x category")
    class DecisionTable {

        @ParameterizedTest(name = "TC-15 {0}: age {1}, licence {2}, {3} -> {4}")
        @CsvSource({
                "R1, 19, 1, ECONOMY,  REJECTED",                // age below 21
                "R2, 76, 10, STANDARD, REJECTED",               // age above 75
                "R3, 30, 0, STANDARD, REJECTED",                // licence below 1 year
                "R4, 22, 2, STANDARD, APPROVED_WITH_SURCHARGE", // young driver, normal car
                "R5, 22, 3, LUXURY,   REJECTED",                // young driver, luxury car
                "R6, 30, 2, LUXURY,   REJECTED",                // adult, luxury, licence < 3
                "R7, 30, 3, LUXURY,   APPROVED",                // adult, luxury, licence >= 3
                "R8, 30, 1, ECONOMY,  APPROVED"                 // adult, normal car
        })
        void decisionTableRules(String rule, int age, int licenceYears,
                                CarCategory category, Eligibility expected) {
            assertEquals(expected, check(age, licenceYears, category), "Rule " + rule);
        }
    }
}
