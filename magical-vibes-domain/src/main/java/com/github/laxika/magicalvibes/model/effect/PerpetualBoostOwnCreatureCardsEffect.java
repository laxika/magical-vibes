package com.github.laxika.magicalvibes.model.effect;

/**
 * Perpetually modifies the power of the controller's creature cards matching a mana-value cap.
 * The affected card identities are recorded by the engine so the modification follows them across
 * zone changes.
 */
public record PerpetualBoostOwnCreatureCardsEffect(int maxManaValue, int powerBoost)
        implements CardEffect {
}
