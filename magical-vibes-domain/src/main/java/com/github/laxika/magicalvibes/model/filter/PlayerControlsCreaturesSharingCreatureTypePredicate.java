package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a player who controls at least {@code minimum} creatures that share a creature type ("defending player
 * controls three or more creatures that share a creature type", Graxiplon). A changeling shares every creature type.
 */
public record PlayerControlsCreaturesSharingCreatureTypePredicate(int minimum) implements PlayerPredicate {

    public PlayerControlsCreaturesSharingCreatureTypePredicate {
        if (minimum < 1) {
            throw new IllegalArgumentException("minimum must be positive");
        }
    }
}
