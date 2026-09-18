package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Each opponent may sacrifice a permanent matching a type predicate carried by the resolving
 * stack entry; an opponent who does not loses life and the effect controller draws a card.
 */
public record EachOpponentMaySacrificeMatchingPermanentOrLoseLifeAndControllerDrawsEffect(int lifeLoss)
        implements CardDrawingEffect {

    public EachOpponentMaySacrificeMatchingPermanentOrLoseLifeAndControllerDrawsEffect() {
        this(2);
    }

    @Override
    public DynamicAmount drawnCardAmount() {
        return new Fixed(1);
    }
}
