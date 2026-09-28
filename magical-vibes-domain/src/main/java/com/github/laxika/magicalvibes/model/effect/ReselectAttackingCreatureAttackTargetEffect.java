package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

/** Prompts for a legal attack target and changes the target of the targeted attacking creature. */
public record ReselectAttackingCreatureAttackTargetEffect(boolean selfTargeting, boolean playersOnly)
        implements CardEffect {

    public ReselectAttackingCreatureAttackTargetEffect() {
        this(false, false);
    }

    @Override
    public TargetSpec targetSpec() {
        if (selfTargeting) {
            return new TargetSpec(null, false, null, true, 1);
        }
        return TargetSpec.benign(TargetPredicates.creature(), new PermanentIsAttackingPredicate());
    }
}
