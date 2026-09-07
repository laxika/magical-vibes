package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

public record ReturnDyingOpponentCreatureAsTreasureEffect(UUID dyingCardId)
        implements CardEffect, DyingCreatureCardAwareEffect {

    public ReturnDyingOpponentCreatureAsTreasureEffect() {
        this(null);
    }

    @Override
    public CardEffect boundToDyingCard(UUID dyingCardId) {
        return new ReturnDyingOpponentCreatureAsTreasureEffect(dyingCardId);
    }
}
