package com.github.laxika.magicalvibes.model.effect;

/**
 * Triggered-ability marker: "Whenever you discard a card, exile that card from your graveyard."
 * Placed in {@link com.github.laxika.magicalvibes.model.EffectSlot#ON_CONTROLLER_DISCARDS}. The
 * just-discarded card (already in the controller's graveyard) is moved to exile. Used by Necropotence.
 */
public record ExileDiscardedCardFromGraveyardEffect() implements CardEffect {
}
