package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Exiles the card of a dying creature and creates a token copy of it when the exile succeeds.
 * The token-copy profile supplies the characteristic exceptions to the copy.
 */
public record ExileDyingCreatureAndCreateTokenCopyEffect(
        UUID dyingCardId,
        CreateTokenCopyOfTargetPermanentEffect tokenCopyEffect
) implements CardEffect, DyingCreatureCardAwareEffect {

    public ExileDyingCreatureAndCreateTokenCopyEffect(
            CreateTokenCopyOfTargetPermanentEffect tokenCopyEffect) {
        this(null, tokenCopyEffect);
    }

    @Override
    public CardEffect boundToDyingCard(UUID dyingCardId) {
        return new ExileDyingCreatureAndCreateTokenCopyEffect(dyingCardId, tokenCopyEffect);
    }
}
