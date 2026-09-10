package com.github.laxika.magicalvibes.model.effect;

/** Copies the cards exiled to pay this ability's cost and offers up to the given number of copies for a free cast. */
public record CopyCardsExiledWithSourceAndCastUpToEffect(int maxCount) implements CardEffect {

    public CopyCardsExiledWithSourceAndCastUpToEffect {
        if (maxCount <= 0) {
            throw new IllegalArgumentException("maxCount must be positive");
        }
    }
}
