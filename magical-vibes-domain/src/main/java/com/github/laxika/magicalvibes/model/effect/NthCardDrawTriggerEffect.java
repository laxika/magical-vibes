package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;
import java.util.Optional;

/**
 * Trigger payload for abilities that care about a player's Nth card draw each turn.
 * It is consumed by the controller- or opponent-draw trigger path and is not resolved directly.
 */
public record NthCardDrawTriggerEffect(int cardNumber, CardEffect resolvedEffect)
        implements DrawTriggerEffect {

    public NthCardDrawTriggerEffect {
        if (cardNumber < 1) {
            throw new IllegalArgumentException("cardNumber must be positive");
        }
        Objects.requireNonNull(resolvedEffect, "resolvedEffect");
    }

    @Override
    public Optional<CardEffect> effectForDrawCount(int cardsDrawnThisTurn) {
        return cardsDrawnThisTurn == cardNumber
                ? Optional.of(resolvedEffect)
                : Optional.empty();
    }

    @Override
    public TargetSpec targetSpec() {
        return resolvedEffect.targetSpec();
    }
}
