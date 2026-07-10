package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static effect: creatures that do NOT match the exemption predicate can't attack this
 * permanent's controller (they may still attack other players). Unlike
 * {@link CreaturesCantAttackUnlessPredicateEffect}, this is a defender-scoped restriction
 * evaluated per attacked player at attack-declaration time.
 * Example: Form of the Dragon — "Creatures without flying can't attack you." The
 * exemptionPredicate would match creatures WITH flying.
 *
 * @param exemptionPredicate creatures matching this predicate ARE allowed to attack the controller
 */
public record CreaturesCantAttackControllerUnlessPredicateEffect(PermanentPredicate exemptionPredicate) implements CardEffect {
}
