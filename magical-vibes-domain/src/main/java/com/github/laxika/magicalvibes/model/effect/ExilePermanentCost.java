package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Activation cost: exile one permanent matching {@code filter}.
 *
 * @param filter the permanents that may be exiled
 * @param description human-readable cost description used in prompts
 * @param excludeSource when true, the ability's own source cannot be exiled
 * @param trackExiledManaValue when true, the exiled permanent's mana value is snapshotted into
 *                             the ability's xValue at payment
 * @param trackWithSource when true, cards exiled for this cost are tracked with the ability's source
 */
public record ExilePermanentCost(PermanentPredicate filter, String description, boolean excludeSource,
                                 boolean trackExiledManaValue, boolean trackWithSource) implements CostEffect {

    public ExilePermanentCost(PermanentPredicate filter, String description) {
        this(filter, description, true, false, false);
    }

    public ExilePermanentCost(PermanentPredicate filter, String description, boolean excludeSource) {
        this(filter, description, excludeSource, false, false);
    }

    public ExilePermanentCost(PermanentPredicate filter, String description, boolean excludeSource,
                              boolean trackExiledManaValue) {
        this(filter, description, excludeSource, trackExiledManaValue, false);
    }

    @Override
    public PermanentPredicate consumedPermanentFilter() {
        return filter;
    }
}
