package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

/**
 * Redirects all damage that the target attacking creature would deal to the controller this turn
 * to the source permanent instead.
 */
public record RedirectAllDamageFromTargetCreatureToSelfEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature(), new PermanentIsAttackingPredicate());
    }
}
