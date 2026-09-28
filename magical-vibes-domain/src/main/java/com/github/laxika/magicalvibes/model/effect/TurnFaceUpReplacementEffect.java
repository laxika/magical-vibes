package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Replacement behavior that modifies how a face-down permanent is turned face up.
 *
 * <p>These effects are applied during the turn-face-up action and never go on the stack.
 */
public interface TurnFaceUpReplacementEffect extends ReplacementEffect {

    DynamicAmount counterAmount();

    default CounterType counterType() {
        return CounterType.PLUS_ONE_PLUS_ONE;
    }

    default boolean appliesWithoutPayingCost() {
        return true;
    }
}
