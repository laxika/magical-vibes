package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;
import java.util.Optional;

/**
 * Trigger payload for abilities that trigger on the first or second card drawn each turn.
 * <p>
 * Not authored on a card: the legacy view ({@code TriggeredAbility.legacyEffects()}) builds it for the collectors
 * from an ability with the conditions {@code youDrawNthCardEachTurn(1)} and {@code youDrawSecondCardEachTurn()}.
 */
public record FirstOrSecondCardDrawTriggerEffect(CardEffect resolvedEffect)
        implements DrawTriggerEffect {

    public FirstOrSecondCardDrawTriggerEffect {
        Objects.requireNonNull(resolvedEffect, "resolvedEffect");
    }

    @Override
    public Optional<CardEffect> effectForDrawCount(int cardsDrawnThisTurn) {
        return cardsDrawnThisTurn >= 1 && cardsDrawnThisTurn <= 2
                ? Optional.of(resolvedEffect)
                : Optional.empty();
    }

    @Override
    public TargetSpec targetSpec() {
        return resolvedEffect.targetSpec();
    }
}
