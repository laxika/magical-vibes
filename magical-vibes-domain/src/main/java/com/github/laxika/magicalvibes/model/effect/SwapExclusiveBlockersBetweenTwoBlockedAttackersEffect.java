package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockedPredicate;

/**
 * General Jarkeld: choose two target blocked attacking creatures. If each could be blocked by
 * all creatures currently blocking the other, each creature blocking exactly one of them stops
 * blocking it and blocks the other instead. Shared blockers (blocking both) are unchanged.
 * Does not cause "becomes blocked" / "blocks" triggers to fire again.
 */
public record SwapExclusiveBlockersBetweenTwoBlockedAttackersEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.CREATURE, new PermanentIsBlockedPredicate());
    }
}
