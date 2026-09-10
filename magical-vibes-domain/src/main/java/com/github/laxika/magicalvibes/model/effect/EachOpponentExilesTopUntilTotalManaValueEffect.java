package com.github.laxika.magicalvibes.model.effect;

/**
 * Each opponent exiles cards from the top of their library until those cards have total mana
 * value at least the configured threshold.
 */
public record EachOpponentExilesTopUntilTotalManaValueEffect(int totalManaValueThreshold)
        implements CardEffect {
}
