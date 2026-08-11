package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Additional cost to cast a spell: put {@code count} counter(s) of the given type on a creature
 * you control (e.g. Scarscale Ritual — "put a -1/-1 counter on a creature you control"). Placed in
 * the {@code SPELL} slot. The creature is supplied via {@code PlayCardRequest.sacrificePermanentId}
 * (reuses the sacrifice-cost id field) and paid in {@code SpellCastingService}. The spell is
 * unplayable if you control no creature.
 *
 * @param counterType type of counter to place
 * @param count number of counters to place
 * @param optional whether the caster may decline this cost
 */
public record PutCounterOnControlledCreatureCost(CounterType counterType, int count, boolean optional)
        implements CostEffect {

    public PutCounterOnControlledCreatureCost(CounterType counterType, int count) {
        this(counterType, count, false);
    }

    private static final PermanentPredicate CREATURE_FILTER = new PermanentIsCreaturePredicate();

    @Override
    public PermanentPredicate consumedPermanentFilter() {
        return CREATURE_FILTER;
    }
}
