package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static blocking restriction on blockers.
 * This creature can block only attackers that match the provided predicate.
 */
public record CanBlockOnlyIfAttackerMatchesPredicateEffect(PermanentPredicate attackerPredicate, String allowedAttackersDescription)
        implements CardEffect {
}
