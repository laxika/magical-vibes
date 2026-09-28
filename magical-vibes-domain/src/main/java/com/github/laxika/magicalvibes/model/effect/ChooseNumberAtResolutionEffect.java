package com.github.laxika.magicalvibes.model.effect;

/**
 * The controller chooses a number in the inclusive range during resolution and stores it as the
 * resolving stack entry's X value for following effects.
 */
public record ChooseNumberAtResolutionEffect(int minNumber, int maxNumber) implements CardEffect {

    public ChooseNumberAtResolutionEffect(int maxNumber) {
        this(0, maxNumber);
    }

    public ChooseNumberAtResolutionEffect {
        if (minNumber < 0 || maxNumber < minNumber) {
            throw new IllegalArgumentException("Invalid number-choice range");
        }
    }
}
