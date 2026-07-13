package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static effect: creatures controlled by the source permanent's controller that do NOT match the
 * exemption predicate can't attack. Unlike {@link CreaturesCantAttackUnlessPredicateEffect} (global,
 * every player) this is scoped to the source's own controller.
 * Example: Evil Eye of Orms-by-Gore — "Non-Eye creatures you control can't attack." The
 * exemptionPredicate would match Eyes.
 *
 * @param exemptionPredicate creatures matching this predicate ARE allowed to attack
 */
public record ControlledCreaturesCantAttackUnlessPredicateEffect(PermanentPredicate exemptionPredicate) implements CardEffect {
}
