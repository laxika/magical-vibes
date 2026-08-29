package com.github.laxika.magicalvibes.model.effect;

/**
 * Schedules a non-targeting triggered ability to trigger at the beginning of the end-of-combat
 * step. The scheduled ability is independent of the source permanent remaining on the battlefield.
 */
public record DelayedEndOfCombatEffect(CardEffect effect) implements CardEffect {
}
