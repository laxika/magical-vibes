package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import java.util.List;

/**
 * Searches the controller's library for one card of each listed subtype, of each listed colour, or of each listed
 * quality ({@link #ofEach}: "an instant card with mana value 3, an instant card with mana value 2, and an instant
 * card with mana value 1", Firemind's Foresight) — one card per subtype, colour or quality, in the given order — then
 * shuffles once. The searcher may always fail to find any of the picks
 * (CR 701.23b), and a subtype or colour with no matching card left in the library is skipped. The whole effect is
 * one library search however many cards are found.
 *
 * <p>With a {@link LibrarySearchDestination#HAND} destination each found card is revealed and put
 * into the controller's hand (Gem of Becoming: Island, Swamp, Mountain; Conflux: a card of each colour). When
 * {@code basicOnly} is set each pick must also be a basic land, so the search finds basic lands of those subtypes
 * only (Yasharn, Implacable Earth: a basic Forest and a basic Plains). With
 * {@link LibrarySearchDestination#BATTLEFIELD} or {@link LibrarySearchDestination#BATTLEFIELD_TAPPED}
 * the whole effect is a single search whose found cards enter the battlefield at the same time
 * (Gaea's Balance: a land card of each basic land type), and with {@link LibrarySearchDestination#GRAVEYARD} the
 * cards of each colour are put into the graveyard together after the last pick (Lotuslight Dancers; a card of
 * several colours fills one pick only). A non-null {@code requiredType} also requires every found card to have that
 * card type; it is only supported for battlefield destinations.
 *
 * @param subtypes     one card of each, in order; empty when searching by colour
 * @param requiredType the card type every found card has, battlefield searches only
 * @param destination  where the found cards go
 * @param basicOnly    whether each subtype pick is a basic land (hand searches only)
 * @param colors       one card of each, in order, instead of subtypes; empty when searching by subtype
 * @param each         one card of each quality, in order, instead of subtypes or colours (hand searches only); empty
 *                     otherwise. A to-hand subtype or colour search is the same search over those qualities
 */
public record SearchLibraryForOneCardOfEachSubtypeEffect(List<CardSubtype> subtypes, CardType requiredType,
                                                         LibrarySearchDestination destination, boolean basicOnly,
                                                         List<CardColor> colors,
                                                         List<ChooseCardEffect.Each> each)
        implements CardEffect {

    public SearchLibraryForOneCardOfEachSubtypeEffect(List<CardSubtype> subtypes, CardType requiredType,
                                                      LibrarySearchDestination destination, boolean basicOnly,
                                                      List<CardColor> colors) {
        this(subtypes, requiredType, destination, basicOnly, colors, List.of());
    }

    public SearchLibraryForOneCardOfEachSubtypeEffect(List<CardSubtype> subtypes, CardType requiredType,
                                                      LibrarySearchDestination destination, boolean basicOnly) {
        this(subtypes, requiredType, destination, basicOnly, List.of());
    }

    public SearchLibraryForOneCardOfEachSubtypeEffect(List<CardSubtype> subtypes, CardType requiredType,
                                                      LibrarySearchDestination destination) {
        this(subtypes, requiredType, destination, false);
    }

    public SearchLibraryForOneCardOfEachSubtypeEffect {
        subtypes = List.copyOf(subtypes);
        colors = List.copyOf(colors);
        each = List.copyOf(each);
        if ((subtypes.isEmpty() ? 0 : 1) + (colors.isEmpty() ? 0 : 1) + (each.isEmpty() ? 0 : 1) != 1) {
            throw new IllegalArgumentException("One card of each subtype, of each colour or of each quality: "
                    + subtypes + colors + each);
        }
        if (!each.isEmpty() && destination != LibrarySearchDestination.HAND) {
            throw new IllegalArgumentException("A search for a card of each quality goes to the hand: " + destination);
        }
        boolean toBattlefield = destination == LibrarySearchDestination.BATTLEFIELD
                || destination == LibrarySearchDestination.BATTLEFIELD_TAPPED;
        if (!colors.isEmpty() && destination != LibrarySearchDestination.HAND
                && destination != LibrarySearchDestination.GRAVEYARD) {
            throw new IllegalArgumentException("A colour search goes to a hand or a graveyard: " + destination);
        }
        if (!subtypes.isEmpty() && !toBattlefield && destination != LibrarySearchDestination.HAND) {
            throw new IllegalArgumentException("Unsupported destination: " + destination);
        }
        if (!toBattlefield && requiredType != null) {
            throw new IllegalArgumentException("A required card type is only supported for battlefield searches");
        }
        if (basicOnly && (destination != LibrarySearchDestination.HAND || subtypes.isEmpty())) {
            throw new IllegalArgumentException("A basic-only restriction is only supported for subtype hand searches");
        }
    }

    /** One revealed card of each subtype to hand (Gem of Becoming). */
    public SearchLibraryForOneCardOfEachSubtypeEffect(List<CardSubtype> subtypes) {
        this(subtypes, null, LibrarySearchDestination.HAND);
    }

    /**
     * "Search your library for a white card, a blue card, ..." into the hand, each revealed (Conflux), or "for a
     * black card, a green card, and a blue card and put those cards into your graveyard" (Lotuslight Dancers).
     */
    public static SearchLibraryForOneCardOfEachSubtypeEffect ofEachColor(List<CardColor> colors,
                                                                         LibrarySearchDestination destination) {
        return new SearchLibraryForOneCardOfEachSubtypeEffect(List.of(), null, destination, false, colors);
    }

    /**
     * "Search your library for [a card of the first quality], [a card of the second], and [...], reveal them, put them
     * into your hand": one revealed card each quality's filter matches, in order (Firemind's Foresight).
     */
    public static SearchLibraryForOneCardOfEachSubtypeEffect ofEach(ChooseCardEffect.Each... each) {
        return new SearchLibraryForOneCardOfEachSubtypeEffect(List.of(), null, LibrarySearchDestination.HAND, false,
                List.of(), List.of(each));
    }
}
