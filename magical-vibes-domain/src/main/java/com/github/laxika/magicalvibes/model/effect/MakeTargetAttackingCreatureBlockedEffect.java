package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsUnblockedAttackingPredicate;

/**
 * "Target unblocked attacking creature becomes blocked." Marks the target as blocked even though no
 * creature is blocking it (this works on creatures that can't be blocked). CR 509.1h: a creature can
 * become blocked without any creature blocking it, and CR 510.1c leaves it with no blockers to assign
 * combat damage to, so it deals none. Its "becomes blocked" triggers fire, per-blocker triggers do not
 * (there is no blocker). Harmful. Dazzling Beauty.
 */
public record MakeTargetAttackingCreatureBlockedEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.CREATURE, new PermanentIsUnblockedAttackingPredicate());
    }
}
