package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

import java.util.Objects;

/**
 * "Each player [may] search[es] their library for up to N [basic land | creature] cards, put[s] them onto the
 * battlefield [tapped] | reveal[s] them, put[s] them into their hand, then shuffle[s]": each player {@code players}
 * names searches their own library in APNAP order, for up to their own {@code count} (evaluated with that player as the
 * controller: "for each creature exiled this way, its controller searches", Winds of Abandon); then what was found
 * moves together and the players who searched shuffle (CR 608.2e, 101.4).
 *
 * <p>With {@code optional} a player who takes no card hasn't searched, so doesn't shuffle ("a player who doesn't want
 * to shuffle their library may choose not to search", Boldwyr Heavyweights' ruling). A search for cards with a stated
 * quality that puts them into a hand reveals them; a search for any card doesn't (Noble Benefactor).
 *
 * <p>{@code TOP_OF_LIBRARY} is "searches their library for a card, then shuffles and puts that card on top" (Scheming
 * Symmetry, Varragoth): each of them in turn searches for any one card, which nobody else sees.
 *
 * @param players     who searches; only {@code targetPlayer()} declares the (benign) player targets
 * @param filter      which cards may be found; {@link CardTruePredicate} for any card
 * @param count       up to how many, for each searcher
 * @param destination {@code BATTLEFIELD}, {@code BATTLEFIELD_TAPPED}, {@code HAND} or {@code TOP_OF_LIBRARY}
 * @param optional    "may search"
 */
public record EachPlayerSearchesLibraryEffect(PlayerSubject players, CardPredicate filter, DynamicAmount count,
                                              LibrarySearchDestination destination, boolean optional)
        implements CardEffect {

    public EachPlayerSearchesLibraryEffect {
        Objects.requireNonNull(players, "players");
        Objects.requireNonNull(filter, "filter");
        Objects.requireNonNull(count, "count");
        if (destination != LibrarySearchDestination.BATTLEFIELD
                && destination != LibrarySearchDestination.BATTLEFIELD_TAPPED
                && destination != LibrarySearchDestination.HAND
                && destination != LibrarySearchDestination.TOP_OF_LIBRARY) {
            throw new IllegalArgumentException("No search of each player's library puts cards into " + destination);
        }
        if (destination == LibrarySearchDestination.TOP_OF_LIBRARY
                && (!(filter instanceof CardTruePredicate) || !new Fixed(1).equals(count) || optional)) {
            throw new IllegalArgumentException("A search to the top of the library is a must, for any one card");
        }
    }

    /** "[Players] may each search their library for up to [count] [filter] cards and put them [destination]." */
    public static EachPlayerSearchesLibraryEffect maySearch(PlayerSubject players, CardPredicate filter,
                                                            DynamicAmount count, LibrarySearchDestination destination) {
        return new EachPlayerSearchesLibraryEffect(players, filter, count, destination, true);
    }

    /** {@link #maySearch(PlayerSubject, CardPredicate, DynamicAmount, LibrarySearchDestination)} for one card. */
    public static EachPlayerSearchesLibraryEffect maySearch(PlayerSubject players, CardPredicate filter,
                                                            LibrarySearchDestination destination) {
        return maySearch(players, filter, new Fixed(1), destination);
    }

    /** "[Players] each search their library for a card, then shuffle and put that card on top." */
    public static EachPlayerSearchesLibraryEffect cardToTop(PlayerSubject players) {
        return new EachPlayerSearchesLibraryEffect(players, new CardTruePredicate(), new Fixed(1),
                LibrarySearchDestination.TOP_OF_LIBRARY, false);
    }

    /** "[Players] search their libraries for up to [count] …": the search isn't a choice, finding cards still is. */
    public static EachPlayerSearchesLibraryEffect mustSearch(PlayerSubject players, CardPredicate filter,
                                                             DynamicAmount count, LibrarySearchDestination destination) {
        return new EachPlayerSearchesLibraryEffect(players, filter, count, destination, false);
    }

    @Override
    public TargetSpec targetSpec() {
        return players.declaresTarget() ? TargetSpec.benign(TargetPredicates.player()) : TargetSpec.NONE;
    }
}
