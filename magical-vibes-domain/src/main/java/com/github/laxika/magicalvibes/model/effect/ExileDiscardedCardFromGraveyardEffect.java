package com.github.laxika.magicalvibes.model.effect;

/**
 * Triggered-ability marker: "Whenever you discard a card, exile that card from your graveyard."
 * Placed in {@link com.github.laxika.magicalvibes.model.EffectSlot#ON_CONTROLLER_DISCARDS}. The
 * just-discarded card (already in the controller's graveyard) is moved to exile.
 *
 * @param trackWithSource whether to resolve the exile as a triggered ability and track the exiled
 *                       card with the triggering permanent
 * @param addStashCounter whether the exiled card receives a stash counter instead of being tracked
 *                         with the triggering permanent
 */
public record ExileDiscardedCardFromGraveyardEffect(boolean trackWithSource, boolean addStashCounter)
        implements CardEffect {

    public ExileDiscardedCardFromGraveyardEffect() {
        this(false, false);
    }

    public ExileDiscardedCardFromGraveyardEffect(boolean trackWithSource) {
        this(trackWithSource, false);
    }

    public static ExileDiscardedCardFromGraveyardEffect withStashCounter() {
        return new ExileDiscardedCardFromGraveyardEffect(true, true);
    }
}
