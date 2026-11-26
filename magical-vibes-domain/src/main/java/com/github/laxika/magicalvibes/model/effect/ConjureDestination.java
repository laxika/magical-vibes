package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Zone;

import java.util.Objects;

/**
 * Where {@link ConjureCardsEffect} puts the cards it conjures ("into your hand", "into your graveyard", "into exile",
 * "into the top five cards of your library at random", "into your library seventh from the top"), a zone of the player
 * who gets them ({@link ConjureCardsEffect#owners()}), or "onto the battlefield [tapped]": every card of one step
 * enters as one batch, under its owner's control, with the entry choices the batch asks (CR 603.6a, 303.4f).
 * A duplicate of a permanent conjured onto the battlefield is {@link ConjureDuplicateOntoBattlefieldEffect}.
 *
 * @param zone             {@link Zone#HAND}, {@link Zone#LIBRARY}, {@link Zone#GRAVEYARD}, {@link Zone#EXILE} or
 *                         {@link Zone#BATTLEFIELD}
 * @param libraryTopCards  for the library: the depth from the top, 1 for its top; {@link Integer#MAX_VALUE} with
 *                         {@code libraryAtRandom} for anywhere in it; 0 for every other zone
 * @param libraryAtRandom  for the library: a random position among its top {@code libraryTopCards} cards, rather than
 *                         that position itself; {@code false} for every other zone
 * @param exiledWithSource for exile: the cards are exiled with the source permanent, for its linked "cards exiled with
 *                         this" ability (CR 607.2a); {@code false} for every other zone
 * @param tapped           for the battlefield: the cards enter tapped; {@code false} for every other zone
 */
public record ConjureDestination(Zone zone, int libraryTopCards, boolean libraryAtRandom, boolean exiledWithSource,
                                 boolean tapped) {

    public ConjureDestination {
        Objects.requireNonNull(zone, "zone");
        if (zone != Zone.HAND && zone != Zone.LIBRARY && zone != Zone.GRAVEYARD && zone != Zone.EXILE
                && zone != Zone.BATTLEFIELD) {
            throw new IllegalArgumentException(
                    "A conjure step puts cards into a hand, a library, a graveyard, exile or onto the battlefield: "
                            + zone);
        }
        if ((zone == Zone.LIBRARY) != (libraryTopCards > 0)) {
            throw new IllegalArgumentException("A library position is given for the library only: " + libraryTopCards);
        }
        if (libraryAtRandom && (zone != Zone.LIBRARY || libraryTopCards < 2)) {
            throw new IllegalArgumentException("A random library position needs two or more cards: " + libraryTopCards);
        }
        if (exiledWithSource && zone != Zone.EXILE) {
            throw new IllegalArgumentException("Only exiled cards are exiled with the source");
        }
        if (tapped && zone != Zone.BATTLEFIELD) {
            throw new IllegalArgumentException("Only cards conjured onto the battlefield enter tapped");
        }
    }

    public ConjureDestination(Zone zone, int libraryTopCards, boolean libraryAtRandom, boolean exiledWithSource) {
        this(zone, libraryTopCards, libraryAtRandom, exiledWithSource, false);
    }

    /** "Onto the battlefield." */
    public static ConjureDestination ontoBattlefield() {
        return new ConjureDestination(Zone.BATTLEFIELD, 0, false, false, false);
    }

    /** "Onto the battlefield tapped." */
    public static ConjureDestination ontoBattlefieldTapped() {
        return new ConjureDestination(Zone.BATTLEFIELD, 0, false, false, true);
    }

    /** "Into your hand." */
    public static ConjureDestination intoHand() {
        return new ConjureDestination(Zone.HAND, 0, false, false);
    }

    /** "Into your graveyard." */
    public static ConjureDestination intoGraveyard() {
        return new ConjureDestination(Zone.GRAVEYARD, 0, false, false);
    }

    /** "Into exile." */
    public static ConjureDestination intoExile() {
        return new ConjureDestination(Zone.EXILE, 0, false, false);
    }

    /** "Into exile", read later as "cards exiled with this [permanent]". */
    public static ConjureDestination intoExileWithSource() {
        return new ConjureDestination(Zone.EXILE, 0, false, true);
    }

    /** "Onto the top of your library." */
    public static ConjureDestination ontoTopOfLibrary() {
        return new ConjureDestination(Zone.LIBRARY, 1, false, false);
    }

    /**
     * "Into your library [, then shuffle]": at a random position anywhere in it, so the position tells nothing even
     * where no shuffle follows.
     */
    public static ConjureDestination intoLibrary() {
        return new ConjureDestination(Zone.LIBRARY, Integer.MAX_VALUE, true, false);
    }

    /** "Into your library [N]th from the top": the {@code position}th card from the top, or the bottom if shorter. */
    public static ConjureDestination intoLibraryFromTop(int position) {
        if (position < 1) {
            throw new IllegalArgumentException("A library position from the top is 1 or more: " + position);
        }
        return new ConjureDestination(Zone.LIBRARY, position, false, false);
    }

    /** "Into the top {@code count} cards of your library at random." */
    public static ConjureDestination intoTopCardsOfLibraryAtRandom(int count) {
        if (count < 2) {
            throw new IllegalArgumentException("A random position among the top cards needs two or more: " + count);
        }
        return new ConjureDestination(Zone.LIBRARY, count, true, false);
    }
}
