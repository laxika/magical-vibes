package com.github.laxika.magicalvibes.model.effect;

/**
 * Puts the stack entry's triggering card (the card put into a graveyard that caused the trigger, such as the creature
 * card of a death trigger) from its owner's graveyard on top or on the bottom of its owner's library, if it is still
 * the same graveyard object when the ability resolves. Mortuary's "your graveyard" and "your library" name the owner
 * too, since it triggers only on cards its controller owns.
 *
 * @param destination which end of the library the card goes to
 */
public record PutTriggeringCardFromGraveyardIntoLibraryEffect(Destination destination) implements CardEffect {

    public PutTriggeringCardFromGraveyardIntoLibraryEffect {
        if (destination == null) {
            throw new IllegalArgumentException("destination cannot be null");
        }
    }

    /** Which end of its owner's library the triggering card is put on. */
    public enum Destination {
        TOP,
        BOTTOM
    }
}
