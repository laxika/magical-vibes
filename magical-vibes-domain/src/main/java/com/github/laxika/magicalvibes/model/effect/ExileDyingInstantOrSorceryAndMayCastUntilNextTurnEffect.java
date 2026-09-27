package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/** Exiles a dying instant or sorcery card and grants its controller a next-turn cast permission. */
public record ExileDyingInstantOrSorceryAndMayCastUntilNextTurnEffect(UUID dyingCardId)
        implements CardEffect, DyingCreatureCardAwareEffect {

    public ExileDyingInstantOrSorceryAndMayCastUntilNextTurnEffect() {
        this(null);
    }

    @Override
    public CardEffect boundToDyingCard(UUID dyingCardId) {
        return new ExileDyingInstantOrSorceryAndMayCastUntilNextTurnEffect(dyingCardId);
    }
}
