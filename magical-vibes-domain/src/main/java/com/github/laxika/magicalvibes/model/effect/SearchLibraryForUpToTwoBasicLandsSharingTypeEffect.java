package com.github.laxika.magicalvibes.model.effect;

/**
 * Searches for up to two basic lands that share a land type and puts them onto the battlefield
 * tapped. Both cards are chosen before either enters; the second pick is constrained by the land
 * types of the first pick.
 */
public record SearchLibraryForUpToTwoBasicLandsSharingTypeEffect() implements CardEffect {
}
