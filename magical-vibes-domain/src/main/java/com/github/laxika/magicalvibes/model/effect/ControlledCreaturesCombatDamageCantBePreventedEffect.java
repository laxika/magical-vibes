package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: combat damage dealt by creatures controlled by this permanent's controller can't
 * be prevented. The controller is evaluated when each damage event is processed.
 */
public record ControlledCreaturesCombatDamageCantBePreventedEffect() implements CardEffect {
}
