package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Taps the target permanent and prevents its activated abilities while it remains tapped. */
public record TapAndLockTargetPermanentWhileTappedEffect(PermanentPredicate targetFilter)
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.permanent(), targetFilter);
    }
}
