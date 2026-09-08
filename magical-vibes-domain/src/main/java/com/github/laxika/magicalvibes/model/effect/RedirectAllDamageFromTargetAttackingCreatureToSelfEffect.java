package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

/** Redirects all damage dealt to the controller by the target attacking creature this turn. */
public record RedirectAllDamageFromTargetAttackingCreatureToSelfEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature(), new PermanentIsAttackingPredicate());
    }
}
