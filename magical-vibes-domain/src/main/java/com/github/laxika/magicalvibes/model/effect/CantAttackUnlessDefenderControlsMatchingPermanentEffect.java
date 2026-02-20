package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static attacking restriction on attackers.
 * This creature can't attack unless defending player controls a permanent matching the given predicate.
 */
public record CantAttackUnlessDefenderControlsMatchingPermanentEffect(PermanentPredicate defenderPermanentPredicate, String requirementDescription)
        implements CardEffect {
}
