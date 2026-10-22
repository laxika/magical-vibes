package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import java.util.List;

/**
 * Searches the controller's library for one card of each listed subtype (one card per subtype, in the
 * given order), then shuffles once. The searcher may always fail to find any of the picks, and a
 * subtype with no matching card left in the library is skipped.
 *
 * <p>With a {@link LibrarySearchDestination#HAND} destination each found card is revealed and put
 * into the controller's hand (Gem of Becoming: Island, Swamp, Mountain). When {@code basicOnly} is set
 * each pick must also be a basic land, so the search finds basic lands of those subtypes only (Yasharn,
 * Implacable Earth: a basic Forest and a basic Plains). With
 * {@link LibrarySearchDestination#BATTLEFIELD} or {@link LibrarySearchDestination#BATTLEFIELD_TAPPED}
 * the whole effect is a single search whose found cards enter the battlefield at the same time
 * (Gaea's Balance: a land card of each basic land type). A non-null {@code requiredType} also
 * requires every found card to have that card type; it is only supported for battlefield
 * destinations.
 */
public record SearchLibraryForOneCardOfEachSubtypeEffect(List<CardSubtype> subtypes, CardType requiredType,
                                                         LibrarySearchDestination destination, boolean basicOnly)
        implements CardEffect {

    public SearchLibraryForOneCardOfEachSubtypeEffect(List<CardSubtype> subtypes, CardType requiredType,
                                                      LibrarySearchDestination destination) {
        this(subtypes, requiredType, destination, false);
    }

    public SearchLibraryForOneCardOfEachSubtypeEffect {
        subtypes = List.copyOf(subtypes);
        boolean toBattlefield = destination == LibrarySearchDestination.BATTLEFIELD
                || destination == LibrarySearchDestination.BATTLEFIELD_TAPPED;
        if (!toBattlefield && destination != LibrarySearchDestination.HAND) {
            throw new IllegalArgumentException("Unsupported destination: " + destination);
        }
        if (!toBattlefield && requiredType != null) {
            throw new IllegalArgumentException("A required card type is only supported for battlefield searches");
        }
        if (basicOnly && destination != LibrarySearchDestination.HAND) {
            throw new IllegalArgumentException("A basic-only restriction is only supported for hand searches");
        }
    }

    /** One revealed card of each subtype to hand (Gem of Becoming). */
    public SearchLibraryForOneCardOfEachSubtypeEffect(List<CardSubtype> subtypes) {
        this(subtypes, null, LibrarySearchDestination.HAND);
    }
}
