package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsUnblockedAttackingPredicate;

/** Redirects combat damage to the controller from the target unblocked attacking creature. */
public record RedirectCombatDamageFromTargetUnblockedCreatureToItsControllerEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature(), new PermanentIsUnblockedAttackingPredicate());
    }
}
