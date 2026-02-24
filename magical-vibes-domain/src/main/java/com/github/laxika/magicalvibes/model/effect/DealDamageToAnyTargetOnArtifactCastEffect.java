package com.github.laxika.magicalvibes.model.effect;

/**
 * Trigger descriptor for "Whenever you cast an artifact spell, you may pay {X}.
 * If you do, [source] deals N damage to any target."
 * <p>
 * Used in the {@code ON_ANY_PLAYER_CASTS_SPELL} slot wrapped in {@link MayEffect}.
 * {@code checkSpellCastTriggers} resolves this into a {@link DealDamageToAnyTargetEffect}.
 */
public record DealDamageToAnyTargetOnArtifactCastEffect(int manaCost, int damage) implements CardEffect {
}
