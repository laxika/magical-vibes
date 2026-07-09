package com.github.laxika.magicalvibes.model.effect;

/**
 * Makes one or more players discard their entire hand. The {@link DiscardRecipient} routes who
 * discards: {@code CONTROLLER} (The Flame of Keld, "Discard your hand"), {@code EACH_PLAYER}
 * (Mindslicer, "each player discards their hand"), etc. Unlike {@link DiscardEffect} there is no
 * count or player choice — every card in the affected hand goes to the graveyard.
 *
 * @param recipient who discards their hand
 */
public record DiscardHandEffect(DiscardRecipient recipient) implements CardEffect {

    /** Discards the controller's own hand. */
    public DiscardHandEffect() {
        this(DiscardRecipient.CONTROLLER);
    }
}
