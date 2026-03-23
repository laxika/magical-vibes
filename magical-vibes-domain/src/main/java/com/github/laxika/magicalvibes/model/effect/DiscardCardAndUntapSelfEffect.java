package com.github.laxika.magicalvibes.model.effect;

/**
 * Discard a card, then untap the source permanent.
 * <p>
 * Commonly wrapped in {@link MayEffect} for "you may discard a card. If you do, untap [source]."
 * <p>
 * Uses {@code pendingUntapAfterDiscardPermanentId} on GameData to track which permanent to untap
 * after the interactive discard completes.
 */
public record DiscardCardAndUntapSelfEffect() implements CardEffect {
}
