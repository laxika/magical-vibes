package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

/**
 * Taps the creatures blocking the target attacking creature and prevents combat damage dealt by
 * that attacker and those blockers for the turn.
 */
public record TapAndPreventCombatDamageByTargetAndBlockersEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature(), new PermanentIsAttackingPredicate());
    }
}
