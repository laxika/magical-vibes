package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles a card from the controller's hand, then untaps the source permanent.
 *
 * <p>Commonly wrapped in {@link MayEffect} for "you may exile a card from your hand. If you do,
 * untap [source]." The hand choice carries the source permanent's id so the untap happens only
 * after a card was actually exiled.</p>
 */
public record ExileCardAndUntapSelfEffect() implements CardEffect {
}
