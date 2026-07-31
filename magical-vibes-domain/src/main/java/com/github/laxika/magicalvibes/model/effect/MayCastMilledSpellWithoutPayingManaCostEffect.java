package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker effect used in the may ability system to indicate that the player may cast the associated
 * card — one of the cards just milled — from its owner's graveyard without paying its mana cost.
 * Queued by {@link MillTargetPlayerAndMayCastMilledSpellEffect} (one PendingMayAbility per eligible
 * milled card); accepting one clears the rest.
 */
public record MayCastMilledSpellWithoutPayingManaCostEffect() implements CardEffect {
}
