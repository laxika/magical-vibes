package com.github.laxika.magicalvibes.model.effect;

/**
 * If the equipped creature shares its name with another creature controlled by the ability's
 * controller or a creature card in that controller's graveyard, records a perpetual power/toughness
 * boost for the equipped creature's card identity.
 */
public record PerpetuallyBoostEquippedCreatureIfNameSharedEffect(int powerBoost, int toughnessBoost)
        implements CardEffect {
}
