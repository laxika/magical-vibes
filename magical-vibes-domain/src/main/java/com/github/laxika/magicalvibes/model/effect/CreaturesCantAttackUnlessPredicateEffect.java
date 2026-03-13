package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static effect: creatures that do NOT match the exemption predicate can't attack.
 * This is a global restriction checked against all permanents on the battlefield.
 * Example: Stormtide Leviathan — "Creatures without flying or islandwalk can't attack."
 * The exemptionPredicate would match creatures WITH flying or islandwalk.
 *
 * @param exemptionPredicate creatures matching this predicate ARE allowed to attack
 */
public record CreaturesCantAttackUnlessPredicateEffect(PermanentPredicate exemptionPredicate) implements CardEffect {
}
