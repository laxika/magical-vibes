package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/** Gives a returned creature permanent the suspected designation. */
public record SuspectReturnedPermanentEffect(UUID returnedCardId)
        implements CardEffect, DyingCreatureCardAwareEffect {

    public SuspectReturnedPermanentEffect() {
        this(null);
    }

    @Override
    public CardEffect boundToDyingCard(UUID dyingCardId) {
        return new SuspectReturnedPermanentEffect(dyingCardId);
    }
}
