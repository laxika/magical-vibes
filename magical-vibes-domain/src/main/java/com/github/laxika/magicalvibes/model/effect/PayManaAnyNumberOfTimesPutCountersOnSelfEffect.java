package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * On resolution, the controller may pay a fixed mana cost any number of times and puts one
 * counter of the specified type on the source permanent for each payment.
 *
 * <p>When present, {@code thenEffect} is put on the stack as a reflexive ability after at least
 * one payment, so its targets are chosen after the payment decision.</p>
 */
public record PayManaAnyNumberOfTimesPutCountersOnSelfEffect(
        String manaCost, CounterType counterType, CardEffect thenEffect) implements CardEffect {

    public PayManaAnyNumberOfTimesPutCountersOnSelfEffect(String manaCost, CounterType counterType) {
        this(manaCost, counterType, null);
    }
}
