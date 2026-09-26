package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Returns a bound dying creature card to the battlefield tapped under the ability controller's
 * control, then makes it a land with the configured name and grants it a colorless mana ability.
 * The returned permanent retains its other characteristics and abilities.
 */
public record ReturnDyingCreatureToBattlefieldAsNamedLandEffect(
        UUID dyingCardId,
        String landName
) implements CardEffect, DyingCreatureCardAwareEffect {

    public ReturnDyingCreatureToBattlefieldAsNamedLandEffect(String landName) {
        this(null, landName);
    }

    @Override
    public CardEffect boundToDyingCard(UUID dyingCardId) {
        return new ReturnDyingCreatureToBattlefieldAsNamedLandEffect(dyingCardId, landName);
    }
}
