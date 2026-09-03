package com.github.laxika.magicalvibes.model.effect;

/**
 * Gives the enchanted creature and every other creature sharing a creature type with it a
 * temporary power/toughness boost.
 */
public record BoostEnchantedAndSharingCreaturesUntilEndOfTurnEffect(
        int powerBoost,
        int toughnessBoost
) implements CardEffect {

    @Override
    public boolean resolvesAgainstAttachedPermanent() {
        return true;
    }
}
