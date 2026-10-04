package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Gives the targeted permanent a permanent layer-6 static effect independent of the resolving
 * source permanent.
 */
public record GrantStaticEffectToTargetEffect(CardEffect staticEffect, CounterType whileCounterRemains)
        implements CardEffect {

    public GrantStaticEffectToTargetEffect(CardEffect staticEffect) {
        this(staticEffect, null);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent());
    }
}
