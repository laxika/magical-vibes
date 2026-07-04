package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Destroys the targeted permanent. Its controller searches their library for a card
 * matching the filter, puts it onto the battlefield, then shuffles.
 *
 * @param searchFilter predicate for cards the controller can find in their library
 * @param may          if true, the search is optional ("may search"); if false, mandatory
 * @param tapped       if true, the found card enters the battlefield tapped
 */
public record DestroyTargetPermanentAndControllerSearchesLibraryToBattlefieldEffect(
        CardPredicate searchFilter,
        boolean may,
        boolean tapped
) implements CardEffect {

    /** Convenience overload: the found card enters untapped (e.g. Ghost Quarter). */
    public DestroyTargetPermanentAndControllerSearchesLibraryToBattlefieldEffect(
            CardPredicate searchFilter, boolean may) {
        this(searchFilter, may, false);
    }

    @Override
    public boolean canTargetPermanent() {
        return true;
    }

    @Override
    public boolean isDamageOrDestruction() {
        return true;
    }
}
