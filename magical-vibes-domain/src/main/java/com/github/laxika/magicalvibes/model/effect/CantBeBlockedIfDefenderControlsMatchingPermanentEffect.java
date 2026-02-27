package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static evasion effect on attackers.
 * This creature can't be blocked as long as defending player controls a permanent matching the given predicate.
 */
public record CantBeBlockedIfDefenderControlsMatchingPermanentEffect(PermanentPredicate defenderPermanentPredicate)
        implements CardEffect {
}
