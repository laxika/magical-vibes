package com.github.laxika.magicalvibes.model.effect;

/**
 * Deals {@code damage} to the end-step player ({@code entry.getTargetId()}) if that player's life
 * total is {@code lifeThreshold} or less. Used for the intervening-if triggered ability "At the
 * beginning of each player's end step, if that player has N or less life, ... deals M damage to
 * that player" (Razor Pendulum).
 *
 * <p>The condition is checked both at trigger time (StepTriggerService's END_STEP handler) and at
 * resolution time (CR 603.4). The {@link EndStepPlayerTargetedEffect} marker makes the trigger bake
 * the end-step player into {@code targetId}.
 */
public record DealDamageToEndStepPlayerIfLifeAtMostEffect(int damage, int lifeThreshold)
        implements EndStepPlayerTargetedEffect {
}
