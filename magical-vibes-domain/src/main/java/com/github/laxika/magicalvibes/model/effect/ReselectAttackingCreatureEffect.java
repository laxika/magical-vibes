package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

/** Reselects the player or permanent an attacking creature is attacking. */
public record ReselectAttackingCreatureEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature(), new PermanentIsAttackingPredicate());
    }
}
