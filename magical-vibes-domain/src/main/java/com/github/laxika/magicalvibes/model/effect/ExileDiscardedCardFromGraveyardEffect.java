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
 * @param grantPlayPermissionUntilEndOfTurn whether the controller may play the exiled card until
 *                                           end of turn
 */
public record ExileDiscardedCardFromGraveyardEffect(
        boolean trackWithSource,
        boolean addStashCounter,
        boolean grantPlayPermissionUntilEndOfTurn
)
        implements CardEffect {

    public ExileDiscardedCardFromGraveyardEffect() {
        this(false, false, false);
    }

    public ExileDiscardedCardFromGraveyardEffect(boolean trackWithSource) {
        this(trackWithSource, false, false);
    }

    public ExileDiscardedCardFromGraveyardEffect(boolean trackWithSource, boolean addStashCounter) {
        this(trackWithSource, addStashCounter, false);
    }

    public static ExileDiscardedCardFromGraveyardEffect withStashCounter() {
        return new ExileDiscardedCardFromGraveyardEffect(true, true, false);
    }

    public static ExileDiscardedCardFromGraveyardEffect withPlayPermission() {
        return new ExileDiscardedCardFromGraveyardEffect(false, false, true);
    }
}
