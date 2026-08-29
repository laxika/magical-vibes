package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

/** Redirects the next combat damage from the target attacking creature to itself. */
public record RedirectNextCombatDamageFromTargetAttackingCreatureToSelfEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature(), new PermanentIsAttackingPredicate());
    }
}
