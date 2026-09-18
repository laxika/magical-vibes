package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;
import java.util.Optional;

/**
 * Draw trigger payload that is suppressed for the first card drawn in a player's draw step.
 */
public record ExceptFirstDrawStepTriggerEffect(CardEffect resolvedEffect)
        implements DrawTriggerEffect {

    public ExceptFirstDrawStepTriggerEffect {
        Objects.requireNonNull(resolvedEffect, "resolvedEffect");
    }

    @Override
    public Optional<CardEffect> effectForDrawCount(int cardsDrawnThisTurn) {
        return Optional.of(resolvedEffect);
    }

    @Override
    public TargetSpec targetSpec() {
        return resolvedEffect.targetSpec();
    }
}
