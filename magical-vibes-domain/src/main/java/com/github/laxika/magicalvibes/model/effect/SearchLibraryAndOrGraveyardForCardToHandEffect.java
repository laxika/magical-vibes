package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * "Search your library and/or graveyard for a [filter] card, reveal it, and put it into your hand. If you search your
 * library this way, shuffle": one matching card the controller owns, chosen across {@code zones}, into their hand.
 * The zones are the controller's library ({@link Zone#LIBRARY}, searched unless a rule prevents it, and then shuffled),
 * graveyard ({@link Zone#GRAVEYARD}), cards outside the game ({@link Zone#OUTSIDE_GAME}, judged as they are, untouched
 * by the game's effects) and face-up cards in exile ({@link Zone#EXILE}). A library card may be left unfound (CR
 * 701.23b); a card in any other zone has to be taken unless the text says "you may" ({@code optional}).
 *
 * @param filter             the card the search looks for
 * @param shuffleAfterSearch whether the search ends with the library being shuffled; a search that is
 *                           one of several instructions in the same effect (Journey for the Elixir) sets
 *                           this only on the last one, so the library is shuffled once (CR 701.23h)
 * @param zones              where the card may come from
 * @param optional           "you may": no card has to be taken
 */
public record SearchLibraryAndOrGraveyardForCardToHandEffect(CardPredicate filter, boolean shuffleAfterSearch,
                                                             Set<Zone> zones, boolean optional) implements CardEffect {

    private static final Set<Zone> SEARCHABLE = EnumSet.of(Zone.LIBRARY, Zone.GRAVEYARD, Zone.OUTSIDE_GAME,
            Zone.EXILE);

    public SearchLibraryAndOrGraveyardForCardToHandEffect {
        zones = Set.copyOf(Objects.requireNonNull(zones, "zones"));
        if (zones.isEmpty() || !SEARCHABLE.containsAll(zones)) {
            throw new IllegalArgumentException("A card is put into a hand from a library, a graveyard, outside the game"
                    + " or exile: " + zones);
        }
    }

    public SearchLibraryAndOrGraveyardForCardToHandEffect(CardPredicate filter, boolean shuffleAfterSearch) {
        this(filter, shuffleAfterSearch, Set.of(Zone.LIBRARY, Zone.GRAVEYARD), false);
    }

    public SearchLibraryAndOrGraveyardForCardToHandEffect(CardPredicate filter) {
        this(filter, true);
    }

    /**
     * "Search your library, graveyard, and/or outside the game for a [filter] card you own, reveal it, and put it into
     * your hand. If you search your library this way, shuffle."
     */
    public static SearchLibraryAndOrGraveyardForCardToHandEffect inZones(CardPredicate filter, Zone... zones) {
        return new SearchLibraryAndOrGraveyardForCardToHandEffect(filter, true, Set.of(zones), false);
    }

    /**
     * "You may reveal a [filter] card you own from outside the game or choose a face-up [filter] card you own in exile.
     * Put that card into your hand."
     */
    public static SearchLibraryAndOrGraveyardForCardToHandEffect mayTakeFrom(CardPredicate filter, Zone... zones) {
        return new SearchLibraryAndOrGraveyardForCardToHandEffect(filter, true, Set.of(zones), true);
    }
}
