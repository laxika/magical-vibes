package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

/** Prompts for a legal attack target and changes the target of the targeted attacking creature. */
public record ReselectAttackingCreatureAttackTargetEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature(), new PermanentIsAttackingPredicate());
    }
}
