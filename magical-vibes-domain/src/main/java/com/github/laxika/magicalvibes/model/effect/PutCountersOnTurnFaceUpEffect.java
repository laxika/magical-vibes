package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Puts the specified counters on a permanent as it is turned face up.
 *
 * <p>This models replacement wording such as "As this creature is turned face up, put five
 * +1/+1 counters on it." It can also represent other counter types and replacements that apply
 * only when the turn-face-up cost is paid. It is deliberately separate from a triggered face-up
 * ability.
 */
public record PutCountersOnTurnFaceUpEffect(CounterType counterType, DynamicAmount counterAmount,
                                            boolean appliesWithoutPayingCost)
        implements TurnFaceUpReplacementEffect {

    public PutCountersOnTurnFaceUpEffect(DynamicAmount counterAmount) {
        this(CounterType.PLUS_ONE_PLUS_ONE, counterAmount, true);
    }

    public PutCountersOnTurnFaceUpEffect(int counterCount) {
        this(new Fixed(counterCount));
    }

    public PutCountersOnTurnFaceUpEffect(CounterType counterType, int counterCount) {
        this(counterType, new Fixed(counterCount), true);
    }

    public PutCountersOnTurnFaceUpEffect(CounterType counterType, int counterCount,
                                         boolean appliesWithoutPayingCost) {
        this(counterType, new Fixed(counterCount), appliesWithoutPayingCost);
    }
}
