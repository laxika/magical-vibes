package com.github.laxika.magicalvibes.model.effect;

/**
 * Grants {@code count} additional combat phase(s) after the current combat phase, with NO additional
 * main phase (Finest Hour: "After this phase, there is an additional combat phase"). Contrast with
 * {@link AdditionalCombatMainPhaseEffect}, which inserts a combat phase followed by a main phase
 * (Relentless Assault). Resolved by incrementing {@code GameData.additionalCombatPhasesOnly}, which
 * the turn engine consumes when leaving END_OF_COMBAT.
 */
public record AdditionalCombatPhaseEffect(int count) implements CardEffect {
}
