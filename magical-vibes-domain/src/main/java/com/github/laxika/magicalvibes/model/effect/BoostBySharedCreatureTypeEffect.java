package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static +1/+1 boost for each other creature sharing a creature type. When {@code filter} is
 * non-null, both the affected creature and counted creatures must match it.
 */
public record BoostBySharedCreatureTypeEffect(PermanentPredicate filter) implements CardEffect {

    public BoostBySharedCreatureTypeEffect() {
        this(null);
    }
}
