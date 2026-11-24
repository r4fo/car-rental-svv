package com.carrental;

/**
 * Small console demo that runs the application with two example customers.
 */
public class Main {

    public static void main(String[] args) {
        EligibilityChecker checker = new EligibilityChecker();
        RentalPriceCalculator calculator = new RentalPriceCalculator(checker);

        // A 23-year-old renting an economy car for 7 days, 1500 km, 90 minutes late.
        RentalRequest youngDriver = new RentalRequest(23, 2, CarCategory.ECONOMY, 7, 1500, 90);
        printBill("Young driver", youngDriver, checker, calculator);

        // A 22-year-old asking for a luxury car: rejected.
        RentalRequest luxuryTooYoung = new RentalRequest(22, 3, CarCategory.LUXURY, 3, 300, 0);
        printBill("Young driver, luxury car", luxuryTooYoung, checker, calculator);
    }

    private static void printBill(String label, RentalRequest request,
                                  EligibilityChecker checker, RentalPriceCalculator calculator) {
        System.out.println("=== " + label + " ===");
        Eligibility eligibility = checker.check(request);
        System.out.println("Eligibility:   " + eligibility);
        if (eligibility == Eligibility.REJECTED) {
            System.out.println();
            return;
        }
        System.out.printf("Base price:    $%.2f%n", calculator.basePrice(request));
        System.out.printf("Discount:     -$%.2f%n", calculator.discount(request));
        System.out.printf("Young driver:  $%.2f%n", calculator.youngDriverFee(request));
        System.out.printf("Mileage fee:   $%.2f%n", calculator.mileageFee(request));
        System.out.printf("Late fee:      $%.2f%n", calculator.lateFee(request));
        System.out.println("Return status: " + calculator.returnStatus(request));
        System.out.printf("TOTAL:         $%.2f%n", calculator.totalPrice(request));
        System.out.printf("Deposit back:  $%.2f%n", calculator.depositRefund(request));
        System.out.printf("Still owed:    $%.2f%n%n", calculator.amountOwedAfterDeposit(request));
    }
}
