package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * The permanent recorded as chosen deals damage equal to its power to each matching creature.
 * The chosen permanent is the damage source for every damage event.
 */
public record ChosenPermanentDealsPowerDamageToEachMatchingCreatureEffect(PermanentPredicate filter)
        implements BoardWipeEffect {

    @Override
    public boolean sweepsBoard() {
        return true;
    }
}
