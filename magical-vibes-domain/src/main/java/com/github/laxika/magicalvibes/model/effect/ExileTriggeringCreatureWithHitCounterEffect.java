package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/** Exiles the creature that died with a hit counter on it. */
public record ExileTriggeringCreatureWithHitCounterEffect(UUID dyingCardId)
        implements CardEffect, DyingCreatureCardAwareEffect {

    public ExileTriggeringCreatureWithHitCounterEffect() {
        this(null);
    }

    @Override
    public CardEffect boundToDyingCard(UUID dyingCardId) {
        return new ExileTriggeringCreatureWithHitCounterEffect(dyingCardId);
    }
}
