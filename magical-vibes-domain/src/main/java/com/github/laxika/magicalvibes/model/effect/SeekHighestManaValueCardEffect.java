package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

import java.util.Objects;

/** Seeks one card with the highest mana value at or below the evaluated maximum. */
public record SeekHighestManaValueCardEffect(DynamicAmount maximumManaValue) implements CardEffect {

    public SeekHighestManaValueCardEffect {
        Objects.requireNonNull(maximumManaValue, "maximumManaValue");
    }
}
