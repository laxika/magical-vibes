package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Phases out a target permanent and prevents its normal phase-in while the source remains tapped.
 */
public record PhaseOutTargetPermanentWhileSourceTappedEffect(PermanentPredicate targetPredicate)
        implements CardEffect {

    public PhaseOutTargetPermanentWhileSourceTappedEffect() {
        this(null);
    }

    @Override
    public TargetSpec targetSpec() {
        return targetPredicate == null
                ? TargetSpec.harmful(TargetPredicates.permanent())
                : TargetSpec.harmful(TargetPredicates.permanent(), targetPredicate);
    }
}
