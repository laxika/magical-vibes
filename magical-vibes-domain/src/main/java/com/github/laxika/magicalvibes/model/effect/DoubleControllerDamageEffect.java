package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;

/**
 * Static effect that doubles damage dealt by sources controlled by this permanent's controller.
 * Unlike {@link DoubleDamageEffect} which doubles all damage globally, this only affects
 * sources controlled by the same player who controls this permanent.
 *
 * <p>The {@code stackFilter} predicate restricts which stack-based damage sources (spells/abilities)
 * are doubled. A {@code null} filter matches all stack entries.
 *
 * <p>The {@code appliesToCombatDamage} flag controls whether combat damage is also doubled.
 *
 * <p>Examples:
 * <ul>
 *   <li>Angrath's Marauders: {@code new DoubleControllerDamageEffect(null, true)} — doubles all damage</li>
 *   <li>Fire Servant: {@code new DoubleControllerDamageEffect(redInstantOrSorceryFilter, false)} — doubles only red instant/sorcery damage</li>
 * </ul>
 */
public record DoubleControllerDamageEffect(StackEntryPredicate stackFilter, boolean appliesToCombatDamage) implements CardEffect {
}
