package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Phases out a target permanent and keeps it phased out until a player planeswalks. */
public record PhaseOutTargetPermanentUntilPlaneswalkEffect(PermanentPredicate targetPredicate)
        implements CardEffect {

    public PhaseOutTargetPermanentUntilPlaneswalkEffect() {
        this(null);
    }

    @Override
    public TargetSpec targetSpec() {
        return targetPredicate == null
                ? TargetSpec.harmful(TargetPredicates.permanent())
                : TargetSpec.harmful(TargetPredicates.permanent(), targetPredicate);
    }
}
