package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Destroys the targeted permanent. Its controller searches their library for a card
 * matching the filter, puts it onto the battlefield, then shuffles.
 *
 * @param searchFilter predicate for cards the controller can find in their library
 * @param may          if true, the search is optional ("may search"); if false, mandatory
 */
public record DestroyTargetPermanentAndControllerSearchesLibraryToBattlefieldEffect(
        CardPredicate searchFilter,
        boolean may
) implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }

    @Override
    public boolean isDamageOrDestruction() {
        return true;
    }
}
