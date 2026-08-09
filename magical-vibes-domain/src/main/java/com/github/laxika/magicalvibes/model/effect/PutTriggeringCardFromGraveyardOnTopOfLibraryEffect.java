package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Death-trigger effect that puts the card of the creature that died from its controller's
 * graveyard on top of that player's library.
 *
 * <p>The card ID is bound when an ally-creature-death trigger is collected, so the effect still
 * identifies the correct card if multiple creatures die together.
 *
 * @param dyingCardId the card ID of the creature that died, or {@code null} in the card definition
 */
public record PutTriggeringCardFromGraveyardOnTopOfLibraryEffect(UUID dyingCardId)
        implements CardEffect, DyingCreatureCardAwareEffect {

    public PutTriggeringCardFromGraveyardOnTopOfLibraryEffect() {
        this(null);
    }

    @Override
    public CardEffect boundToDyingCard(UUID dyingCardId) {
        return new PutTriggeringCardFromGraveyardOnTopOfLibraryEffect(dyingCardId);
    }
}
