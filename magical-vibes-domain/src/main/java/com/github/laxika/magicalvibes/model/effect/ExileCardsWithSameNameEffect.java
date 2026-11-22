package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Zone;

import java.util.List;

/**
 * "Search [a player's] graveyard, hand, and library for [any number of | all] cards with the same name as [that
 * object] and exile them. Then that player shuffles", "[that player] reveals their hand and exiles all cards with that
 * name from their hand and graveyard", "exile all permanents with that name": exiles the cards and permanents in
 * {@code zones} that share a name with what the effect names (CR 201.2a), as one step that records its exile and how
 * many of the cards came from a hand.
 *
 * <p>What is named is either permanents, each as it is or as the exile step before this one last saw it (CR 608.2h;
 * {@link Subjects#targetSpell()} is the spell as it is or was countered), or cards ({@code cardsThisWay(EXILE)},
 * {@code targetCard(...)}: the effect then declares that target).
 *
 * @param permanents whose names are searched for; {@code null} when {@code cards} names them
 * @param cards      whose names are searched for; {@code null} when {@code permanents} names them
 * @param searched   whose zones are searched; {@code null} for the owners of the named cards
 * @param zones      the zones searched; a library searched is shuffled afterwards
 * @param find       how much of what matches is exiled
 * @param withSource the exiled cards are exiled with the source permanent
 */
public record ExileCardsWithSameNameEffect(PermanentSubject permanents, CardSubject cards, PlayerSubject searched,
                                           List<Zone> zones, Find find, boolean withSource) implements CardEffect {

    /** How much of what matches is exiled. */
    public enum Find {
        /** "Search ... for any number of cards": the effect's controller chooses, in every zone. */
        ANY_NUMBER,
        /**
         * "Search ... for all cards": each one in a public zone is found, and the effect's controller chooses in the
         * hidden ones, which needn't be searched exhaustively (CR 701.23b).
         */
        ALL,
        /** "Exile all cards with that name": each one, a hand being revealed first; nothing is searched. */
        EVERY
    }

    public ExileCardsWithSameNameEffect {
        zones = List.copyOf(zones);
        if ((permanents == null) == (cards == null)) {
            throw new IllegalArgumentException("Either permanents or cards name what is searched for");
        }
        if (searched == null && cards == null) {
            throw new IllegalArgumentException("Only a named card's owner is searched unnamed");
        }
        if (zones.isEmpty() || find != Find.EVERY && zones.contains(Zone.BATTLEFIELD)
                || find == Find.EVERY && zones.contains(Zone.LIBRARY)) {
            throw new IllegalArgumentException("A library is searched and the battlefield is not: " + find + " " + zones);
        }
    }

    private static final List<Zone> GRAVEYARD_HAND_LIBRARY = List.of(Zone.GRAVEYARD, Zone.HAND, Zone.LIBRARY);

    /** "Search [searched]'s graveyard, hand, and library for any number of cards with the same name as it." */
    public static ExileCardsWithSameNameEffect anyNumber(PermanentSubject named, PlayerSubject searched) {
        return new ExileCardsWithSameNameEffect(named, null, searched, GRAVEYARD_HAND_LIBRARY, Find.ANY_NUMBER, false);
    }

    /** "Search its owner's graveyard, hand, and library for any number of cards with the same name as that card." */
    public static ExileCardsWithSameNameEffect anyNumber(CardSubject named) {
        return new ExileCardsWithSameNameEffect(null, named, null, GRAVEYARD_HAND_LIBRARY, Find.ANY_NUMBER, false);
    }

    /** "Search [searched]'s graveyard, hand, and library for all cards with the same name as it." */
    public static ExileCardsWithSameNameEffect all(PermanentSubject named, PlayerSubject searched) {
        return new ExileCardsWithSameNameEffect(named, null, searched, GRAVEYARD_HAND_LIBRARY, Find.ALL, false);
    }

    /** "Search its owner's graveyard, hand, and library for all cards with the same name as that card." */
    public static ExileCardsWithSameNameEffect all(CardSubject named) {
        return new ExileCardsWithSameNameEffect(null, named, null, GRAVEYARD_HAND_LIBRARY, Find.ALL, false);
    }

    /** This effect over the zones of {@code players}. */
    public ExileCardsWithSameNameEffect searching(PlayerSubject players, Zone... in) {
        return new ExileCardsWithSameNameEffect(permanents, cards, players, List.of(in), find, withSource);
    }

    /** This effect exiling all that match, with no search ({@link Find#EVERY}). */
    public static ExileCardsWithSameNameEffect every(PermanentSubject named, PlayerSubject searched, Zone... in) {
        return new ExileCardsWithSameNameEffect(named, null, searched, List.of(in), Find.EVERY, false);
    }

    /** This effect exiling all that match, with no search, named by cards. */
    public static ExileCardsWithSameNameEffect every(CardSubject named, PlayerSubject searched, Zone... in) {
        return new ExileCardsWithSameNameEffect(null, named, searched, List.of(in), Find.EVERY, false);
    }

    /** These cards exiled with the source permanent. */
    public ExileCardsWithSameNameEffect exiledWithSource() {
        return new ExileCardsWithSameNameEffect(permanents, cards, searched, zones, find, true);
    }

    @Override
    public TargetSpec targetSpec() {
        if (cards instanceof CardSubject.TargetCard target) {
            return TargetSpec.benign(target.filter() == null
                    ? TargetPredicates.graveyardCard(target.scope())
                    : TargetPredicates.graveyardCards(target.filter(), target.scope()));
        }
        return TargetSpec.NONE;
    }
}
