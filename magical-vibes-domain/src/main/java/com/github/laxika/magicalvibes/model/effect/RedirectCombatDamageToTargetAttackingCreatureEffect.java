package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

/** Redirects combat damage that would be dealt to the controller to the target attacking creature. */
public record RedirectCombatDamageToTargetAttackingCreatureEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature(), new PermanentIsAttackingPredicate());
    }
}
