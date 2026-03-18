package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Marker effect for ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER slot.
 * When a creature controlled by the same player deals combat damage to a player,
 * put counters on that creature if it matches the predicate.
 * A null predicate means any creature qualifies.
 */
public record PutCountersOnDamageDealerEffect(int powerModifier, int toughnessModifier, int amount,
                                              PermanentPredicate predicate) implements CardEffect {
}
