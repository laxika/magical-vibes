package com.github.laxika.magicalvibes.model.condition;

/** The controller controls a prime number of lands. */
public record ControllerControlsPrimeNumberOfLands() implements Condition {

    @Override
    public String conditionName() {
        return "a prime number of lands";
    }

    @Override
    public String conditionNotMetReason() {
        return "controller does not control a prime number of lands";
    }
}
