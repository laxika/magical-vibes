package com.github.laxika.magicalvibes.model.effect;

/** Performs the specified number of time-travel events. */
public record TimeTravelEffect(int times) implements CardEffect {

    public TimeTravelEffect {
        if (times < 1) {
            throw new IllegalArgumentException("times must be positive");
        }
    }
}
