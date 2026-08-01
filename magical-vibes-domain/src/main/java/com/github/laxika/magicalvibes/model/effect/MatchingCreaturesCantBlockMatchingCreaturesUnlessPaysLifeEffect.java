package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Global static effect: while the source permanent is on the battlefield, any creature matching
 * {@code blockerPredicate} can't block any creature matching {@code attackerPredicate} unless the
 * blocker's controller pays {@code lifePerBlocker} life for each such blocking creature
 * (Heat Wave — nonblue creatures can't block creatures you control unless their controller pays
 * 1 life for each blocking creature they control). Both predicates use the imposing permanent's
 * {@code FilterContext}. Charged once per unique qualifying blocker at declare-blockers time via
 * {@link GlobalBlockLifeCostEffect}.
 *
 * @param blockerPredicate which creatures pay the life tax to block (the "nonblue creatures")
 * @param attackerPredicate which attackers they can't block for free (e.g. "creatures you control")
 * @param lifePerBlocker    life paid per unique qualifying blocker
 * @param description       short human-readable phrase for logs / UI
 */
public record MatchingCreaturesCantBlockMatchingCreaturesUnlessPaysLifeEffect(
        PermanentPredicate blockerPredicate,
        PermanentPredicate attackerPredicate,
        int lifePerBlocker,
        String description) implements GlobalBlockLifeCostEffect {

    @Override
    public PermanentPredicate lifeCostBlockerMatcher() {
        return blockerPredicate;
    }

    @Override
    public PermanentPredicate lifeCostAttackerMatcher() {
        return attackerPredicate;
    }
}
