package com.github.laxika.magicalvibes.model.effect;

/**
 * Destroys the permanent whose event produced this triggered ability — "destroy it" where "it" is
 * the permanent that entered (or otherwise fired the trigger), not a chosen target.
 *
 * <p>Reads {@code StackEntry.triggeringPermanentId}. Never targets; if the permanent has left the
 * battlefield, nothing happens. Pass {@code cannotBeRegenerated=true} for "It can't be regenerated."
 * Used by Suleiman's Legacy on {@code ON_ANY_PERMANENT_ENTERS_BATTLEFIELD}.
 */
public record DestroyTriggeringPermanentEffect(boolean cannotBeRegenerated) implements CardEffect {

    public DestroyTriggeringPermanentEffect() {
        this(false);
    }
}
