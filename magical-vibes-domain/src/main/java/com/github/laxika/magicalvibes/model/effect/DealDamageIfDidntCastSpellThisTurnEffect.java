package com.github.laxika.magicalvibes.model.effect;

/**
 * Deals {@code damage} to the end-step player ({@code entry.getTargetId()}) if that
 * player didn't cast a spell this turn. Used for the intervening-if triggered ability
 * "At the beginning of each player's end step, if that player didn't cast a spell this
 * turn, ... deals N damage to that player" (Impatience).
 *
 * <p>The condition is checked both at trigger time (StepTriggerService's END_STEP handler,
 * which also bakes the end-step player into {@code targetId}) and at resolution time. A
 * countered spell still counts as having been cast (it is recorded when cast).
 */
public record DealDamageIfDidntCastSpellThisTurnEffect(int damage) implements CardEffect {
}
