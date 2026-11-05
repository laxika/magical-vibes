package com.github.laxika.magicalvibes.model.effect;

/**
 * Wrapper for a triggered ability that fires only the first time each creature is affected during
 * a turn: the counter-placement watcher keys the limit by the source permanent and the affected
 * creature, so separate creatures can each trigger the wrapped ability once. Don't author it on a
 * damage or counter trigger: {@code TriggerLimit.ONCE_EACH_TURN_FOR_EACH_CREATURE} says it, and the damage and counter
 * collectors judge it from the ability. "For the first time during your turn" on a tap trigger is {@code
 * Triggers.becomesTappedForFirstTimeDuringYourTurn()} or {@code
 * Triggers.creatureYouControlBecomesTappedForFirstTimeDuringYourTurn()}, which the tap collector judges from the
 * ability.
 */
public record OncePerTurnPerCreatureTriggerEffect(CardEffect wrapped) implements CardEffect {
}
