package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

import java.util.Objects;

/**
 * Discards the controller's entire hand, draws a fixed number of cards, then resolves a
 * reflexive follow-up only if at least one card was discarded.
 */
public record DiscardOwnHandThenDrawAndThenEffect(int drawCount, CardEffect thenEffect)
        implements CardDrawingEffect {

    public DiscardOwnHandThenDrawAndThenEffect {
        if (drawCount < 0) {
            throw new IllegalArgumentException("drawCount must not be negative");
        }
        Objects.requireNonNull(thenEffect, "thenEffect");
    }

    @Override
    public DynamicAmount drawnCardAmount() {
        return new Fixed(drawCount);
    }
}
