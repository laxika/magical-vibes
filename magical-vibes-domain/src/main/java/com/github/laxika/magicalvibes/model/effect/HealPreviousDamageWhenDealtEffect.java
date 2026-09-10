package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement effect that heals all previously marked damage from its permanent when damage
 * is dealt to it.
 */
public record HealPreviousDamageWhenDealtEffect() implements DamageHealingEffect {
}
