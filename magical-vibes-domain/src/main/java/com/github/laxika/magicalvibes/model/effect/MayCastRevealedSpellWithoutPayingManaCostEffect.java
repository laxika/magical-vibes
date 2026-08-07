package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker effect used in the may ability system to indicate that the player may cast the associated
 * card — one of the cards just revealed and still held outside every zone — without paying its mana
 * cost. Queued by {@link RevealTopCardsOfTargetPlayerAndCastInstantOrSorceryEffect} (one
 * PendingMayAbility per revealed instant or sorcery); the group's remaining cast count and held
 * cards live in {@code PendingInteraction.RevealedFreeCastGroup}.
 */
public record MayCastRevealedSpellWithoutPayingManaCostEffect() implements CardEffect {
}
