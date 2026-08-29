package com.github.laxika.magicalvibes.model.effect;

/**
 * Inserts additional combat phases immediately after the resolving main phase, with no main phase
 * between them. The turn engine returns to the phase that would normally follow that main phase
 * after the inserted combats.
 */
public record AdditionalCombatPhasesAfterMainEffect(int count) implements CardEffect {
}
