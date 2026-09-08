package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Puts every matching permanent on top of its owner's library. Each owner chooses the order of
 * their matching permanents when they have more than one.
 */
public record PutMatchingPermanentsOnTopOfOwnersLibrariesEffect(PermanentPredicate filter)
        implements BoardWipeEffect {

    @Override
    public boolean sweepsBoard() {
        return true;
    }
}
