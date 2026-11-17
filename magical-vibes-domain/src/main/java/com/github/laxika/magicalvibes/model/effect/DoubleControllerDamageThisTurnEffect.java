package com.github.laxika.magicalvibes.model.effect;

/**
 * One-shot effect: for the rest of the turn, if a source the spell's controller controls would deal
 * damage, it deals double that damage instead. Resolving adds a floating effect on the controller that
 * lasts until end of turn, applied in {@code GameQueryService.getControllerDamageMultiplier} for both combat and
 * noncombat damage. Multiple instances stack multiplicatively. Used by Insult (Insult // Injury).
 */
public record DoubleControllerDamageThisTurnEffect() implements CardEffect, DoublingEffect {
}
