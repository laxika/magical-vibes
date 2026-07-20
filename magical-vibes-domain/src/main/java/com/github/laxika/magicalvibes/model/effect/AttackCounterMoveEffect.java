package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker for an {@code ON_ATTACK} triggered ability that carries <em>two</em> permanent targets chosen
 * sequentially as the ability goes on the stack: first a creature the attacking player controls, then
 * up to one creature the defending player controls (Decimator Beetle). The ordinary attack-trigger
 * pipeline collects only a single target, so {@code CombatAttackService} routes any trigger whose
 * effects implement this marker into the bespoke two-step
 * {@code PermanentChoiceContext.AttackCounterMoveFirstTarget} / {@code AttackCounterMoveSecondTarget}
 * flow instead. The chosen ids land on the stack entry's flat {@code targetIds} list
 * (position 0 = the controller's creature, position 1 = the optional defending-player creature).
 *
 * <p>Declared as an interface so branching on it in the (non-exempt) combat service does not add an
 * effect-instanceof to the dispatch ratchet.
 */
public interface AttackCounterMoveEffect extends CardEffect {
}
