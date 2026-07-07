package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.condition.Condition;

/**
 * Static attacking restriction: this creature can't attack unless the given {@link Condition}
 * is met. Evaluated at attack-declaration legality time in the combat service via
 * {@code ConditionEvaluationService}.
 * <p>
 * Examples:
 * <ul>
 *   <li>Desperate Castaways — {@code ControlsPermanentCount(1, artifact)} ("unless you control an artifact")</li>
 *   <li>Sea Monster — {@code DefendingPlayerControlsPermanent(Island)} ("unless defending player controls an Island")</li>
 *   <li>Harbor Serpent — {@code AnyPlayerControlsPermanentCount(5, Island)} ("unless there are five or more Islands on the battlefield")</li>
 *   <li>Chained Throatseeker — {@code DefendingPlayerPoisoned()} ("unless defending player is poisoned")</li>
 *   <li>Bloodcrazed Goblin — {@code OpponentDealtDamageThisTurn()} ("unless an opponent was dealt damage this turn")</li>
 * </ul>
 *
 * @param condition              the condition that must be met for this creature to attack
 * @param requirementDescription human-readable "unless" clause (e.g. "you control an artifact")
 */
public record CantAttackUnlessEffect(Condition condition, String requirementDescription) implements CardEffect {
}
