package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Additional cast cost: sacrifice a creature, discard a card, or pay a fixed amount of life.
 * Exactly one option is paid through the cast request's permanent or hand-card selection.
 */
public record SacrificeCreatureOrDiscardCardOrPayLifeCost(int lifeAmount) implements CostEffect {

    private static final PermanentPredicate CREATURE_FILTER = new PermanentIsCreaturePredicate();

    @Override
    public PermanentPredicate consumedPermanentFilter() {
        return CREATURE_FILTER;
    }

    @Override
    public boolean sacrificesChosenCreature() {
        return true;
    }
}
