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
 * {@link Subjects#targetSpell()} is the spell as it is or was countered), cards ({@code cardsThisWay(EXILE)},
 * {@code targetCard(...)}: the effect then declares that target), or, with neither, the card name chosen earlier in
 * the resolution ({@link #anyNumberWithChosenName}, "a card with that name": one of its names is the chosen one,
 * CR 709.4a); a searched {@code targetPlayer()} is then the target declared. "All cards with the same name as the
 * chosen card" exiles a named card that is in the hand searched whatever else is found (Lobotomy's ruling). When no
 * card is named and a player's hand and library are searched, the searcher still gets the search, for nothing
 * (CR 701.23a; Lobotomy's and Pick the Brain's rulings).
 *
 * @param permanents whose names are searched for; {@code null} when cards or the chosen name name them
 * @param cards      whose names are searched for; {@code null} when permanents or the chosen name name them
 * @param searched   whose zones are searched; {@code null} for the owners of the named cards
 * @param zones      the zones searched; a library searched is shuffled afterwards
 * @param find       how much of what matches is exiled
 * @param withSource the exiled cards are exiled with the source permanent
 * @param maxCount   "up to four cards": how many may be found; {@link Integer#MAX_VALUE} for no bound
 */
public record ExileCardsWithSameNameEffect(PermanentSubject permanents, CardSubject cards, PlayerSubject searched,
                                           List<Zone> zones, Find find, boolean withSource,
                                           int maxCount) implements CardEffect {

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
        if (permanents != null && cards != null) {
            throw new IllegalArgumentException("Permanents, cards or the chosen name name what is searched for");
        }
        if (maxCount < 1 || maxCount != Integer.MAX_VALUE && find != Find.ANY_NUMBER) {
            throw new IllegalArgumentException("Only a search for up to a number of cards has a bound: " + maxCount);
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
        return new ExileCardsWithSameNameEffect(named, null, searched, GRAVEYARD_HAND_LIBRARY, Find.ANY_NUMBER, false,
                Integer.MAX_VALUE);
    }

    /** "Search its owner's graveyard, hand, and library for any number of cards with the same name as that card." */
    public static ExileCardsWithSameNameEffect anyNumber(CardSubject named) {
        return new ExileCardsWithSameNameEffect(null, named, null, GRAVEYARD_HAND_LIBRARY, Find.ANY_NUMBER, false,
                Integer.MAX_VALUE);
    }

    /** "Search [searched]'s graveyard, hand, and library for all cards with the same name as it." */
    public static ExileCardsWithSameNameEffect all(PermanentSubject named, PlayerSubject searched) {
        return new ExileCardsWithSameNameEffect(named, null, searched, GRAVEYARD_HAND_LIBRARY, Find.ALL, false,
                Integer.MAX_VALUE);
    }

    /** "Search its owner's graveyard, hand, and library for all cards with the same name as that card." */
    public static ExileCardsWithSameNameEffect all(CardSubject named) {
        return new ExileCardsWithSameNameEffect(null, named, null, GRAVEYARD_HAND_LIBRARY, Find.ALL, false,
                Integer.MAX_VALUE);
    }

    /** "Search [searched]'s graveyard, hand, and library for any number of cards with that name", the chosen one. */
    public static ExileCardsWithSameNameEffect anyNumberWithChosenName(PlayerSubject searched) {
        return upToWithChosenName(Integer.MAX_VALUE, searched);
    }

    /** "Search [searched]'s graveyard, hand, and library for up to [count] cards with that name", the chosen one. */
    public static ExileCardsWithSameNameEffect upToWithChosenName(int count, PlayerSubject searched) {
        return new ExileCardsWithSameNameEffect(null, null, searched, GRAVEYARD_HAND_LIBRARY, Find.ANY_NUMBER, false,
                count);
    }

    /** "Search [searched]'s graveyard, hand, and library for all cards with that name", the chosen one. */
    public static ExileCardsWithSameNameEffect allWithChosenName(PlayerSubject searched) {
        return new ExileCardsWithSameNameEffect(null, null, searched, GRAVEYARD_HAND_LIBRARY, Find.ALL, false,
                Integer.MAX_VALUE);
    }

    /** This effect over the zones of {@code players}. */
    public ExileCardsWithSameNameEffect searching(PlayerSubject players, Zone... in) {
        return new ExileCardsWithSameNameEffect(permanents, cards, players, List.of(in), find, withSource, maxCount);
    }

    /** This effect exiling all that match, with no search ({@link Find#EVERY}). */
    public static ExileCardsWithSameNameEffect every(PermanentSubject named, PlayerSubject searched, Zone... in) {
        return new ExileCardsWithSameNameEffect(named, null, searched, List.of(in), Find.EVERY, false,
                Integer.MAX_VALUE);
    }

    /** This effect exiling all that match, with no search, named by cards. */
    public static ExileCardsWithSameNameEffect every(CardSubject named, PlayerSubject searched, Zone... in) {
        return new ExileCardsWithSameNameEffect(null, named, searched, List.of(in), Find.EVERY, false,
                Integer.MAX_VALUE);
    }

    /** These cards exiled with the source permanent. */
    public ExileCardsWithSameNameEffect exiledWithSource() {
        return new ExileCardsWithSameNameEffect(permanents, cards, searched, zones, find, true, maxCount);
    }

    @Override
    public TargetSpec targetSpec() {
        if (cards instanceof CardSubject.TargetCard target) {
            return TargetSpec.benign(target.filter() == null
                    ? TargetPredicates.graveyardCard(target.scope())
                    : TargetPredicates.graveyardCards(target.filter(), target.scope()));
        }
        if (permanents == null && cards == null && searched.declaresTarget()) {
            return TargetSpec.benign(TargetPredicates.player());
        }
        return TargetSpec.NONE;
    }
}
