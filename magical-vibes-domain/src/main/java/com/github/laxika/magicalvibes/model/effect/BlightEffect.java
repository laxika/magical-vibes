package com.github.laxika.magicalvibes.model.effect;

/**
 * Performs the blight action by putting {@code count} -1/-1 counters on a creature the controller
 * controls, then puts the optional reflexive effect on the stack if the action was performed.
 * Wrap this in {@link MayEffect} for "you may blight N".
 *
 * @param count      number of -1/-1 counters to put on the chosen creature
 * @param thenEffect effect that follows a successful blight action, or {@code null}
 * @param thenEffectTargets whether the follow-up is a new targeted reflexive ability rather than
 *                          an effect that resolves against the blighted creature
 */
public record BlightEffect(int count, CardEffect thenEffect, boolean thenEffectTargets) implements CardEffect {

    public BlightEffect(int count, CardEffect thenEffect) {
        this(count, thenEffect, false);
    }
}
