package com.github.laxika.magicalvibes.model.effect;

/**
 * Trigger descriptor for "Whenever you cast an artifact spell, you may put a
 * charge counter on this artifact."
 * <p>
 * Used in the {@code ON_ANY_PLAYER_CASTS_SPELL} slot wrapped in {@link MayEffect}.
 * {@code checkSpellCastTriggers} resolves this into a {@link PutChargeCounterOnSelfEffect}.
 */
public record PutChargeCounterOnSelfOnArtifactCastEffect() implements CardEffect {
}
