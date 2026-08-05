package com.github.laxika.magicalvibes.model.effect;

/**
 * Phyrexian Portal: if the controller's library has ten or more cards in it, the target opponent
 * looks at the top ten cards of that library and separates them into two face-down piles. One pile
 * is exiled; the other is searched for a card that goes to the controller's hand, and the rest of
 * that pile is shuffled back into the library.
 *
 * <p>Reuses the shared card-pile separation flow
 * ({@link com.github.laxika.magicalvibes.model.PendingPileSeparation} with
 * {@link com.github.laxika.magicalvibes.model.CardPileDisposition#SEARCH_ONE_TO_HAND}): the
 * opponent assigns the piles, then the controller picks which pile to keep. Because the piles are
 * face down, the controller sees only their card counts when picking.
 */
public record PhyrexianPortalEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
