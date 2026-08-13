package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static additional attack cost: this creature can't attack unless its controller returns
 * matching permanents to their owners' hands as attackers are declared.
 *
 * @param count       number of permanents to return
 * @param filter      which permanents may be returned
 * @param description human-readable description of the cost
 */
public record CantAttackUnlessReturnToHandEffect(
        int count,
        PermanentPredicate filter,
        String description
) implements CardEffect {
}
