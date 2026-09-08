package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Exiles the card of a dying creature and creates a Spirit token copy of it.
 * The dying card ID is bound by the ally-death trigger collector.
 */
public record ExileDyingCreatureAndCreateSpiritTokenCopyEffect(UUID dyingCardId)
        implements CardEffect, DyingCreatureCardAwareEffect {

    public ExileDyingCreatureAndCreateSpiritTokenCopyEffect() {
        this(null);
    }

    @Override
    public CardEffect boundToDyingCard(UUID dyingCardId) {
        return new ExileDyingCreatureAndCreateSpiritTokenCopyEffect(dyingCardId);
    }
}
