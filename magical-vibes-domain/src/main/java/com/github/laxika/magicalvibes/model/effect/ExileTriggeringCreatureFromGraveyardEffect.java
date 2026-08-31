package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Exiles the creature card that caused a creature-death trigger from its owner's graveyard.
 *
 * @param dyingCardId the dying creature's card ID, bound when the death trigger is collected
 */
public record ExileTriggeringCreatureFromGraveyardEffect(UUID dyingCardId)
        implements CardEffect, DyingCreatureCardAwareEffect {

    public ExileTriggeringCreatureFromGraveyardEffect() {
        this(null);
    }

    @Override
    public CardEffect boundToDyingCard(UUID dyingCardId) {
        return new ExileTriggeringCreatureFromGraveyardEffect(dyingCardId);
    }
}
