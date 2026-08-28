package com.github.laxika.magicalvibes.model.effect;

/**
 * On resolution, the controller may pay a fixed mana cost up to a specified number of times.
 * When at least one payment is made, {@code thenEffect} is put on the stack as a reflexive
 * ability after the payment decision.
 */
public record PayManaUpToNTimesEffect(String manaCost, int maximumPayments, CardEffect thenEffect)
        implements CardEffect {

    public PayManaUpToNTimesEffect {
        if (maximumPayments < 1) {
            throw new IllegalArgumentException("maximumPayments must be positive");
        }
    }
}
