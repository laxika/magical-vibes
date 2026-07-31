package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;

/**
 * Static replacement effect: if a matching source controlled by this permanent's controller would
 * deal damage to a permanent or player, it deals that much damage plus {@code amount} instead.
 *
 * <p>The {@code stackFilter} predicate restricts which stack-based damage sources (spells/abilities)
 * receive the bonus. A {@code null} filter matches all stack entries. Combat damage is never
 * affected — unlike {@link DoubleControllerDamageEffect} there is no combat flag.
 *
 * <p>Multiple instances stack additively. Only applies when the source would deal at least 1 damage.
 * Queried by {@code GameQueryService.getControllerDamageBonus} from
 * {@code applyDamageMultiplier(GameData, int, StackEntry)}.
 *
 * <p>Example: Pyromancer's Gauntlet —
 * {@code new AdditionalControllerDamageEffect(2, redInstantOrSorceryOrRedPlaneswalkerFilter)}.
 */
public record AdditionalControllerDamageEffect(int amount, StackEntryPredicate stackFilter) implements CardEffect {
}
