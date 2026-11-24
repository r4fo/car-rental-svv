# Car Rental Eligibility and Pricing System

SVV Research Assignment, Rafik Ajam (2309116215).

A small Java console application that decides whether a customer may rent a car and
calculates the rental bill (base price, discount, young driver fee, mileage fee, late fee,
return status and the $300 deposit refund). It is tested with JUnit 5 and its coverage
is measured with JaCoCo.

## Project structure

| Path | Contents |
| --- | --- |
| `src/main/java/com/carrental/` | Application code (7 classes) |
| `src/test/java/com/carrental/` | JUnit 5 tests (3 test classes, TC-01 to TC-25, 72 executions) |
| `pom.xml` | Maven configuration (Java 17, JUnit 5, JaCoCo 0.8.14) |

## How to run

Requirements: JDK 17 or newer and Maven.

```
mvn test
```

Runs all 72 tests and writes the coverage report to `target/site/jacoco/index.html`.

```
mvn compile
java -cp target/classes com.carrental.Main
```

Runs the console demo with two example customers.
