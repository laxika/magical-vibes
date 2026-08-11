package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Additional cast cost: put counters on a creature you control or pay a mana cost. Exactly one
 * option is paid; the creature, when chosen, is supplied through the spell's shared
 * {@code sacrificePermanentId} cast selection.
 */
public record PutCountersOnControlledCreatureOrPayManaCost(CounterType counterType, int count,
                                                            String manaCost) implements CostEffect {

    private static final PermanentPredicate CREATURE_FILTER = new PermanentIsCreaturePredicate();

    @Override
    public PermanentPredicate consumedPermanentFilter() {
        return CREATURE_FILTER;
    }
}
