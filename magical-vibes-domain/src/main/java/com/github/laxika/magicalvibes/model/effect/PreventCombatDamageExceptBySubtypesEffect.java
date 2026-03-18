package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Prevents all combat damage this turn dealt by creatures that do NOT match the given predicate.
 * Creatures that match the predicate are exempt and deal combat damage normally.
 * <p>
 * Example: Moonmist — "Prevent all combat damage that would be dealt this turn
 * by creatures other than Werewolves and Wolves."
 *
 * @param exemptPredicate creatures matching this predicate are NOT prevented from dealing combat damage
 */
public record PreventCombatDamageExceptBySubtypesEffect(PermanentPredicate exemptPredicate) implements CardEffect {
}
