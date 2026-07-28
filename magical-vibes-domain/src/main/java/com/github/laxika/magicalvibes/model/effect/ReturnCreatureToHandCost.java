package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Additional cost to cast a spell: return a creature you control to its owner's hand
 * (e.g. Familiar's Ruse). Placed in the {@code SPELL} slot. The creature to return is
 * supplied via {@code PlayCardRequest.sacrificePermanentId} and paid in
 * {@code SpellCastingService}. The spell is unplayable if you control no creature.
 */
public record ReturnCreatureToHandCost() implements CostEffect {

    private static final PermanentPredicate CREATURE_FILTER = new PermanentIsCreaturePredicate();

    @Override
    public PermanentPredicate consumedPermanentFilter() {
        return CREATURE_FILTER;
    }
}
