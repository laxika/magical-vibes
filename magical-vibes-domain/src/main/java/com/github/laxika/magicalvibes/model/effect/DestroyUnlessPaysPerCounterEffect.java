package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * At the beginning of your upkeep, destroy this permanent unless you pay {@code costPerCounter}
 * for each counter of {@code counterType} on it (Musician's music-counter ability).
 *
 * @param counterType     counter kind that scales the payment (e.g. {@link CounterType#MUSIC})
 * @param costPerCounter  mana cost paid once per counter (e.g. {@code "{1}"})
 */
public record DestroyUnlessPaysPerCounterEffect(CounterType counterType, String costPerCounter)
        implements CardEffect {
}
