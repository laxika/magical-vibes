package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/** As-enters replacement that rolls dice and puts counters equal to their total on the permanent. */
public record RollDiceAndEnterWithCountersEffect(
        DynamicAmount diceCount,
        int sides,
        CounterType counterType
) implements ReplacementEffect {

    public RollDiceAndEnterWithCountersEffect {
        if (diceCount == null) {
            throw new IllegalArgumentException("RollDiceAndEnterWithCountersEffect requires a dice count");
        }
        if (sides < 2) {
            throw new IllegalArgumentException("RollDiceAndEnterWithCountersEffect requires at least two sides");
        }
        if (counterType == null) {
            throw new IllegalArgumentException("RollDiceAndEnterWithCountersEffect requires a counter type");
        }
    }
}
