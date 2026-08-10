package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches an opponent whose hand has at least {@code minimumDifference} more cards than the
 * evaluating controller's hand.
 *
 * @param minimumDifference       the required hand-size difference
 * @param recheckAtResolution     whether the hand-size comparison must still hold at resolution
 */
public record PlayerHasMoreCardsInHandThanControllerPredicate(
        int minimumDifference,
        boolean recheckAtResolution
) implements PlayerPredicate {

    public PlayerHasMoreCardsInHandThanControllerPredicate(int minimumDifference) {
        this(minimumDifference, false);
    }
}
