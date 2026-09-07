package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Capability for static effects that prevent a fixed amount of damage from each source to
 * matching creatures controlled by the source's controller.
 */
public interface FilteredCreaturesDamagePreventionEffect extends CardEffect {

    /** Returns the predicate selecting the protected creatures, or {@code null} for all creatures. */
    PermanentPredicate filter();

    /** Returns the amount prevented from each matching damage event. */
    int amount();
}
