package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Unscythe, Killer of Kings: "you may exile that card. If you do, create a 2/2 black Zombie creature
 * token." Placed (wrapped in a {@link MayEffect}) in the {@code ON_DAMAGED_CREATURE_DIES} slot.
 *
 * <p>The dying creature's card id is bound at trigger-collection time; the {@code token} blueprint is
 * supplied by the card. Resolution exiles the dying card from its owner's graveyard only if it is
 * still there — per the official rulings, a token or a card that has already left the graveyard can't
 * be exiled, and if the exile doesn't happen no token is created.
 *
 * @param dyingCardId the card id of the dying creature (null in the card definition, bound at trigger time)
 * @param token       the token to create when the exile succeeds
 */
public record ExileDyingCreatureCardAndCreateTokenEffect(UUID dyingCardId, CreateTokenEffect token)
        implements CardEffect, DyingCreatureCardAwareEffect {

    public ExileDyingCreatureCardAndCreateTokenEffect(CreateTokenEffect token) {
        this(null, token);
    }

    @Override
    public CardEffect boundToDyingCard(UUID dyingCardId) {
        return new ExileDyingCreatureCardAndCreateTokenEffect(dyingCardId, token);
    }
}
