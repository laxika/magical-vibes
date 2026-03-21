package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * At the beginning of the controller's end step, if at least {@code minRequired}
 * permanents opponents control have a counter of type {@code counterType} on them,
 * destroy one of those permanents at random.
 *
 * <p>This is an intervening-if triggered ability: the condition is checked both
 * when the trigger would go on the stack and again on resolution.
 *
 * <p>Used by Haphazard Bombardment: "At the beginning of your end step, if two or
 * more permanents you don't control have an aim counter on them, destroy one of
 * those permanents at random."
 */
public record DestroyRandomOpponentPermanentWithCounterEffect(
        CounterType counterType,
        int minRequired
) implements CardEffect {
}
