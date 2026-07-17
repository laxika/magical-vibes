package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static effect on a permanent (any controller): every creature matching {@code matcher}
 * must attack each combat if able, regardless of who controls it. Global analogue of the
 * self-only {@link MustAttackEffect}, read in
 * {@code CombatAttackService.getMustAttackRequirementCount}. Per CR 508.1d the controller
 * is not required to pay any attack costs even when this effect is present.
 * Used by Goblin Assault (Goblin creatures).
 */
public record MatchingCreaturesMustAttackEffect(PermanentPredicate matcher) implements CardEffect {
}
