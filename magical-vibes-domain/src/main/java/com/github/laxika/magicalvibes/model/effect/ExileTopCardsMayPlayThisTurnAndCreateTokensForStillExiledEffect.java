package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the top cards of the controller's library, grants permission to play them until end of
 * turn, and creates a delayed trigger for cards from this resolution that remain exiled.
 */
public record ExileTopCardsMayPlayThisTurnAndCreateTokensForStillExiledEffect(
        int count,
        CreateTokenEffect tokenEffect
) implements CardEffect {

    public ExileTopCardsMayPlayThisTurnAndCreateTokensForStillExiledEffect {
        if (count <= 0) {
            throw new IllegalArgumentException("count must be positive");
        }
        if (tokenEffect == null) {
            throw new IllegalArgumentException("tokenEffect must not be null");
        }
    }
}
