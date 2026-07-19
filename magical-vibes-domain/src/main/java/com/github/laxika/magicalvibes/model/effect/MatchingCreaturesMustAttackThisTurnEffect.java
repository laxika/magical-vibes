package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * One-shot, non-targeted: every creature matching {@code matcher} (any controller) must attack
 * this turn if able. Sets the transient {@code mustAttackThisTurn} flag on each matching creature,
 * cleared at end of turn via {@code resetModifiers()}. Read in
 * {@code CombatAttackService.getMustAttackRequirementCount}, so the requirement only bites when a
 * matching creature can legally attack (i.e. its controller is the active player during combat).
 * The one-shot analogue of the static {@link MatchingCreaturesMustAttackEffect}; the predicate is
 * evaluated with the effect's source controller as {@code sourceControllerId}, so
 * "creatures your opponents control attack this turn if able" is this effect with
 * {@code PermanentNotPredicate(PermanentControlledBySourceControllerPredicate())} (Suicidal Charge).
 */
public record MatchingCreaturesMustAttackThisTurnEffect(PermanentPredicate matcher) implements CardEffect {
}
