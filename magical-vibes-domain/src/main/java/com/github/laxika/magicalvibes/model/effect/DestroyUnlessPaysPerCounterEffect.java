package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * At the beginning of your upkeep, destroy (or sacrifice) this permanent unless you pay
 * {@code costPerCounter} for each counter of {@code counterType} on it.
 *
 * <p>Musician's music-counter ability is the destroy variant; Phantasmal Sphere is the sacrifice
 * variant. Sacrifice matters: it ignores regeneration and indestructible, destruction does not.</p>
 *
 * @param counterType    counter kind that scales the payment (e.g. {@link CounterType#MUSIC})
 * @param costPerCounter mana cost paid once per counter (e.g. {@code "{1}"})
 * @param sacrifice      {@code true} to sacrifice the permanent when unpaid, {@code false} to destroy it
 */
public record DestroyUnlessPaysPerCounterEffect(CounterType counterType, String costPerCounter, boolean sacrifice)
        implements CardEffect {

    /** Destroy variant (Musician). */
    public DestroyUnlessPaysPerCounterEffect(CounterType counterType, String costPerCounter) {
        this(counterType, costPerCounter, false);
    }
}
