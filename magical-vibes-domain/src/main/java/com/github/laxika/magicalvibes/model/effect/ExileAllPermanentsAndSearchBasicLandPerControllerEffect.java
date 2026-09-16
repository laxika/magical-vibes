package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Exiles all matching permanents, then has each matched permanent's controller search for a basic
 * land card and put it onto the battlefield tapped.
 */
public record ExileAllPermanentsAndSearchBasicLandPerControllerEffect(PermanentPredicate filter)
        implements BoardWipeEffect {

    @Override
    public boolean sweepsBoard() {
        return true;
    }
}
