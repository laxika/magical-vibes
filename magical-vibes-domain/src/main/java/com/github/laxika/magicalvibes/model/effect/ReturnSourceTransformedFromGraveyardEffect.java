package com.github.laxika.magicalvibes.model.effect;

/**
 * Returns the source card from its owner's graveyard to the battlefield transformed immediately
 * on resolution. By default it enters under the trigger controller's control; when
 * {@code underOwnerControl} is true, it enters under its owner's control.
 *
 * <p>Unlike {@link RegisterDelayedReturnSourceTransformedEffect} there is no wait for the next
 * end step.
 */
public record ReturnSourceTransformedFromGraveyardEffect(boolean underOwnerControl) implements CardEffect {

    public ReturnSourceTransformedFromGraveyardEffect() {
        this(false);
    }
}
