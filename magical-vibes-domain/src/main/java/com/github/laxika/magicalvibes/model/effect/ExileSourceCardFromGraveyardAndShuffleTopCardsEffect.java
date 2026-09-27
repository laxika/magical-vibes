package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the source card from its graveyard, then shuffles up to {@code count} cards from the
 * ability controller's library and puts that pile back on top.
 */
public record ExileSourceCardFromGraveyardAndShuffleTopCardsEffect(int count) implements CardEffect {

    public ExileSourceCardFromGraveyardAndShuffleTopCardsEffect {
        if (count < 0) {
            throw new IllegalArgumentException("count cannot be negative");
        }
    }
}
