package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: damage dealt to the controller does not cause them to lose life.
 * The damage is still dealt, so damage triggers and other damage consequences still apply.
 */
public record DamageDoesNotCauseLifeLossEffect() implements CardEffect {
}
