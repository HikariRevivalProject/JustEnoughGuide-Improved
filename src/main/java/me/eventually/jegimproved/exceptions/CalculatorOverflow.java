package me.eventually.jegimproved.exceptions;

public class CalculatorOverflow extends RuntimeException {
    public CalculatorOverflow(String message) {
        super(message);
    }
}
