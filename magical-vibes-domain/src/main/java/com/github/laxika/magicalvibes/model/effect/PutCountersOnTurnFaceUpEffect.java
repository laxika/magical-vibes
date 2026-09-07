package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Puts the specified number of +1/+1 counters on a permanent as it is turned face up.
 *
 * <p>This models replacement wording such as "As this creature is turned face up, put five
 * +1/+1 counters on it." It is deliberately separate from a triggered face-up ability.
 */
public record PutCountersOnTurnFaceUpEffect(DynamicAmount counterAmount) implements TurnFaceUpReplacementEffect {

    public PutCountersOnTurnFaceUpEffect(int counterCount) {
        this(new Fixed(counterCount));
    }
}
