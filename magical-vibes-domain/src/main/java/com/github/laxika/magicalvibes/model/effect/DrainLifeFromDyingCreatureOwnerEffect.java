package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Makes the owner of the creature that caused an ally-creature-death trigger lose life, then
 * makes the triggered ability's controller gain the same amount.
 */
public record DrainLifeFromDyingCreatureOwnerEffect(int amount, UUID dyingCardId)
        implements CardEffect, DyingCreatureCardAwareEffect {

    public DrainLifeFromDyingCreatureOwnerEffect(int amount) {
        this(amount, null);
    }

    @Override
    public CardEffect boundToDyingCard(UUID dyingCardId) {
        return new DrainLifeFromDyingCreatureOwnerEffect(amount, dyingCardId);
    }
}
