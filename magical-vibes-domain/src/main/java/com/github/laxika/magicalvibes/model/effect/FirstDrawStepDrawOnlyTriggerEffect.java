package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;
import java.util.Optional;

/**
 * Draw trigger payload that applies only to the first card a player draws during their own draw
 * step ("Whenever you draw your first card during each of your draw steps"). Counterpart of
 * {@link ExceptFirstDrawStepTriggerEffect}.
 * <p>
 * Not authored on a card: the legacy view ({@code TriggeredAbility.legacyEffects()}) builds it for the collectors
 * from {@code Triggers.youDrawFirstCardInDrawStep()}.
 */
public record FirstDrawStepDrawOnlyTriggerEffect(CardEffect resolvedEffect)
        implements DrawTriggerEffect {

    public FirstDrawStepDrawOnlyTriggerEffect {
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
