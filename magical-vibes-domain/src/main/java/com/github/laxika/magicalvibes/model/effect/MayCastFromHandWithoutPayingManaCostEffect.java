package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker effect used in the may ability system to indicate that the player may cast the
 * associated card from their hand without paying its mana cost.
 * Used by Counterlash (one PendingMayAbility per eligible hand card).
 */
public record MayCastFromHandWithoutPayingManaCostEffect() implements CardEffect {
}
