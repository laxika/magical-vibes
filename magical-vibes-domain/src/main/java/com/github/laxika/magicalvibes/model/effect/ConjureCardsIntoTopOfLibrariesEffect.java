package com.github.laxika.magicalvibes.model.effect;

/**
 * Conjures copies of a registered card printing into the top {@code depth} cards of every
 * player's library at random.
 */
public record ConjureCardsIntoTopOfLibrariesEffect(
        String setCode,
        String collectorNumber,
        int amount,
        int depth
) implements CardEffect {

    public ConjureCardsIntoTopOfLibrariesEffect {
        if (amount < 0) {
            throw new IllegalArgumentException("amount must not be negative");
        }
        if (depth < 1) {
            throw new IllegalArgumentException("depth must be positive");
        }
    }
}
