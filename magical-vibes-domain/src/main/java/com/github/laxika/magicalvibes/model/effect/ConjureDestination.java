package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Zone;

import java.util.Objects;

/**
 * Where {@link ConjureCardsEffect} puts the cards it conjures, always a zone of the effect's controller ("into your
 * graveyard", "into exile", "into the top five cards of your library at random"). Conjuring onto the battlefield is
 * {@link ConjureDuplicateOntoBattlefieldEffect} or {@link ConjureCardToBattlefieldEffect}, which enter through the
 * entry batch.
 *
 * @param zone             {@link Zone#LIBRARY}, {@link Zone#GRAVEYARD} or {@link Zone#EXILE}
 * @param libraryTopCards  for the library: 1 for its top, N for a random position among its top N cards; 0 for every
 *                         other zone
 * @param exiledWithSource for exile: the cards are exiled with the source permanent, for its linked "cards exiled with
 *                         this" ability (CR 607.2a); {@code false} for every other zone
 */
public record ConjureDestination(Zone zone, int libraryTopCards, boolean exiledWithSource) {

    public ConjureDestination {
        Objects.requireNonNull(zone, "zone");
        if (zone != Zone.LIBRARY && zone != Zone.GRAVEYARD && zone != Zone.EXILE) {
            throw new IllegalArgumentException("A conjure step puts cards into a library, a graveyard or exile: "
                    + zone);
        }
        if ((zone == Zone.LIBRARY) != (libraryTopCards > 0)) {
            throw new IllegalArgumentException("A library position is given for the library only: " + libraryTopCards);
        }
        if (exiledWithSource && zone != Zone.EXILE) {
            throw new IllegalArgumentException("Only exiled cards are exiled with the source");
        }
    }

    /** "Into your graveyard." */
    public static ConjureDestination intoGraveyard() {
        return new ConjureDestination(Zone.GRAVEYARD, 0, false);
    }

    /** "Into exile." */
    public static ConjureDestination intoExile() {
        return new ConjureDestination(Zone.EXILE, 0, false);
    }

    /** "Into exile", read later as "cards exiled with this [permanent]". */
    public static ConjureDestination intoExileWithSource() {
        return new ConjureDestination(Zone.EXILE, 0, true);
    }

    /** "Onto the top of your library." */
    public static ConjureDestination ontoTopOfLibrary() {
        return new ConjureDestination(Zone.LIBRARY, 1, false);
    }

    /** "Into the top {@code count} cards of your library at random." */
    public static ConjureDestination intoTopCardsOfLibraryAtRandom(int count) {
        if (count < 2) {
            throw new IllegalArgumentException("A random position among the top cards needs two or more: " + count);
        }
        return new ConjureDestination(Zone.LIBRARY, count, false);
    }
}
