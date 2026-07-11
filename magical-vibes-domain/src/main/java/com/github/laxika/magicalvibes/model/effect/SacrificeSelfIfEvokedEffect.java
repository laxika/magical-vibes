package com.github.laxika.magicalvibes.model.effect;

/**
 * Evoke sacrifice (MTG Rule 702.75e): "When this permanent enters, if its evoke cost was paid,
 * sacrifice it." Placed in the {@code ON_ENTER_BATTLEFIELD} slot; the ETB pipeline
 * ({@code EtbEffectResolver}) resolves it to a {@link SacrificeSelfEffect} when the permanent was
 * cast for its evoke (alternate) cost, and drops the trigger entirely otherwise (intervening-if,
 * CR 603.4). It therefore never reaches stack resolution as itself.
 */
public record SacrificeSelfIfEvokedEffect() implements CardEffect {
}
