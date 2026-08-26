package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Death-trigger effect that puts the card of the creature that died on the bottom of its owner's
 * library.
 *
 * @param dyingCardId the card ID of the creature that died, or {@code null} in the card definition
 */
public record PutTriggeringCardFromGraveyardOnBottomOfLibraryEffect(UUID dyingCardId)
        implements CardEffect, DyingCreatureCardAwareEffect {

    public PutTriggeringCardFromGraveyardOnBottomOfLibraryEffect() {
        this(null);
    }

    @Override
    public CardEffect boundToDyingCard(UUID dyingCardId) {
        return new PutTriggeringCardFromGraveyardOnBottomOfLibraryEffect(dyingCardId);
    }
}
