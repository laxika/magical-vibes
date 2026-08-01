package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Capability for a board-wide static life tax on declaring blockers: creatures matching
 * {@link #lifeCostBlockerMatcher()} can't block creatures matching {@link #lifeCostAttackerMatcher()}
 * unless the blocker's controller pays {@link #lifePerBlocker()} life for each such blocking
 * creature (Heat Wave). Predicates are evaluated with the imposing permanent's {@code FilterContext}.
 * Read at declare-blockers time by {@code CombatBlockService}; the block stays legal, only the
 * life cost gates it.
 */
public interface GlobalBlockLifeCostEffect extends CardEffect {

    PermanentPredicate lifeCostBlockerMatcher();

    PermanentPredicate lifeCostAttackerMatcher();

    int lifePerBlocker();
}
