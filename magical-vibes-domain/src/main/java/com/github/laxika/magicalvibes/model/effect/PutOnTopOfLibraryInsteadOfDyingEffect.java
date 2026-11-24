package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * Replacement effect (CR 614.1a): "If [this creature] would die, put it on top of its owner's library instead", "on
 * the bottom of its owner's library", "shuffle it into your library": the creature goes to its owner's library and
 * never dies (CR 614.6). Its "you may" form (Pulmonic Sliver's grant) is asked through the may prompt, on top only.
 *
 * <p>Checked in {@code GraveyardService.addCardToGraveyard()} when the card would be put into the graveyard from the
 * battlefield (i.e. "die"). Gravebane Zombie, Nissa's Chosen, Saiba Syphoner, Pulmonic Sliver.</p>
 *
 * @param mayChoose whether its controller may decline it
 * @param position  where in the library it goes
 */
public record PutOnTopOfLibraryInsteadOfDyingEffect(boolean mayChoose, Position position) implements CardEffect {

    /** Where in its owner's library the creature goes instead. */
    public enum Position {
        /** "On top of its owner's library". */
        TOP,
        /** "On the bottom of its owner's library". */
        BOTTOM,
        /** "Shuffle it into your library": the library is shuffled. */
        SHUFFLED
    }

    public PutOnTopOfLibraryInsteadOfDyingEffect {
        Objects.requireNonNull(position, "position");
        if (mayChoose && position != Position.TOP) {
            throw new IllegalArgumentException("Only the replacement onto the top is optional: " + position);
        }
    }

    /** "Put it on top of its owner's library instead", optional when {@code mayChoose}. */
    public PutOnTopOfLibraryInsteadOfDyingEffect(boolean mayChoose) {
        this(mayChoose, Position.TOP);
    }

    /** "Put it on top of its owner's library instead". */
    public PutOnTopOfLibraryInsteadOfDyingEffect() {
        this(false);
    }

    /** "Put it on the bottom of its owner's library instead". */
    public static PutOnTopOfLibraryInsteadOfDyingEffect onBottom() {
        return new PutOnTopOfLibraryInsteadOfDyingEffect(false, Position.BOTTOM);
    }

    /** "Shuffle it into your library instead". */
    public static PutOnTopOfLibraryInsteadOfDyingEffect shuffledIn() {
        return new PutOnTopOfLibraryInsteadOfDyingEffect(false, Position.SHUFFLED);
    }
}
