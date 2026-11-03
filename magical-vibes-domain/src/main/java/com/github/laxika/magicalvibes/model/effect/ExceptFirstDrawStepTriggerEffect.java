package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;
import java.util.Optional;

/**
 * Draw trigger payload that is suppressed for the first card drawn in a player's draw step.
 * <p>
 * Not authored on a card: the legacy view ({@code TriggeredAbility.legacyEffects()}) builds it for the collectors
 * from {@code Triggers.opponentDrawsCardExceptFirstInDrawStep()}.
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
