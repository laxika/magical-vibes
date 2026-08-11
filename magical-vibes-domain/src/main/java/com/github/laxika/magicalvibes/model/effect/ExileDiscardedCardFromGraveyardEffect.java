package com.github.laxika.magicalvibes.model.effect;

/**
 * Triggered-ability marker: "Whenever you discard a card, exile that card from your graveyard."
 * Placed in {@link com.github.laxika.magicalvibes.model.EffectSlot#ON_CONTROLLER_DISCARDS}. The
 * just-discarded card (already in the controller's graveyard) is moved to exile.
 *
 * @param trackWithSource whether to track the exiled card with the triggering permanent
 */
public record ExileDiscardedCardFromGraveyardEffect(boolean trackWithSource) implements CardEffect {

    public ExileDiscardedCardFromGraveyardEffect() {
        this(false);
    }
}
