package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Phases out a target permanent and prevents it from phasing in while the resolving ability's
 * controller continues to control the source permanent.
 */
public record PhaseOutTargetPermanentWhileSourceControlledEffect(PermanentPredicate targetPredicate)
        implements CardEffect {

    public PhaseOutTargetPermanentWhileSourceControlledEffect() {
        this(null);
    }

    @Override
    public TargetSpec targetSpec() {
        return targetPredicate == null
                ? TargetSpec.harmful(TargetPredicates.permanent())
                : TargetSpec.harmful(TargetPredicates.permanent(), targetPredicate);
    }
}
