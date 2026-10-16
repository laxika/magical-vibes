package com.github.laxika.magicalvibes.model.effect;

/**
 * Each other planeswalker the source's controller controls has all loyalty abilities of the source,
 * including loyalty abilities the source itself gained from other effects (Kasmina, Enigma Sage).
 * Applied in a dedicated layer 6 pass once every other ability grant has been applied.
 */
public record ShareLoyaltyAbilitiesWithOtherPlaneswalkersEffect() implements CardEffect {
}
