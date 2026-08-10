package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches an opponent whose graveyard has at least {@code minimumDifference} fewer creature
 * cards than the evaluating controller's graveyard.
 *
 * @param minimumDifference   the required creature-card difference
 * @param recheckAtResolution whether the comparison must still hold at resolution
 */
public record PlayerHasFewerCreatureCardsInGraveyardThanControllerPredicate(
        int minimumDifference,
        boolean recheckAtResolution
) implements PlayerPredicate {

    public PlayerHasFewerCreatureCardsInGraveyardThanControllerPredicate(int minimumDifference) {
        this(minimumDifference, false);
    }
}
