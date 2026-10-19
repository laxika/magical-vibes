package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static +1/+1 boost for each other creature sharing a creature type. When {@code filter} is
 * non-null, both the affected creature and counted permanents must match it, and counted
 * permanents need not be creatures (e.g. Sliver Legion counts noncreature Kindred Slivers).
 */
public record BoostBySharedCreatureTypeEffect(PermanentPredicate filter) implements CardEffect {

    public BoostBySharedCreatureTypeEffect() {
        this(null);
    }
}
