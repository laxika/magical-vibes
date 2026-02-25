package com.github.laxika.magicalvibes.model.effect;

/**
 * Trigger descriptor for "Whenever you sacrifice another permanent, you may pay {X}.
 * If you do, [source] deals N damage to any target."
 * <p>
 * Used in the {@code ON_ALLY_PERMANENT_SACRIFICED} slot wrapped in {@link MayEffect}.
 * {@code checkAllyPermanentSacrificedTriggers} resolves this into a {@link DealDamageToAnyTargetEffect}.
 */
public record DealDamageToAnyTargetOnSacrificeEffect(int manaCost, int damage) implements CardEffect {
}
