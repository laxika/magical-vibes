package com.github.laxika.magicalvibes.model;

import java.util.List;
import java.util.UUID;

/**
 * Carry-over work attached to a {@link PendingInteraction.LibrarySearch} (via
 * {@link LibrarySearchParams#followUp()}) and performed (or continued) when the search
 * completes. Replaces the per-mechanic {@code pending*} fields {@code GameData} used to hold:
 * {@code basicLandToHand} begins the second Cultivate-style pick (basic land to hand);
 * {@code cardToGraveyard} begins the second Final Parting pick (any card to graveyard);
 * the each-player basic-land pair is the APNAP remainder of an "each player searches for a basic
 * land" flow (Field of Ruin, Old-Growth Dryads), advanced after each player's search resolves;
 * the each-player creature-to-hand pair is the APNAP remainder of an "each player may search for up
 * to N creature cards to hand" flow (Weird Harvest), carrying the shared per-player count;
 * the each-player creature-to-battlefield list is the APNAP remainder of an "each opponent may
 * search for a creature card to battlefield" flow (Boldwyr Heavyweights);
 * {@code opponentExileChoice} prompts the opponent after the Distant Memories exile;
 * {@code imprintSourcePermanentId} receives the imprinted card at EXILE_IMPRINT completion;
 * {@code secondBoundedPick} begins the second of two bounded picks (may reveal a card of its type
 * from the same looked-at cards to hand, then dispose the rest) after the first pick resolves —
 * Gift of the Gargantuan (land, rest on bottom), Benefaction of Rhonas (enchantment, rest to
 * graveyard);
 * {@code remainingSameNamePicks} is the queue of permanent names still to search for, one entry
 * per chosen permanent, in a "for each chosen permanent, you may search for a card with the same
 * name and put it onto the battlefield tapped" flow (Clarion Ultimatum) — after each single-name
 * pick resolves the next name in the queue begins its own search;
 * {@code remainingColorToHandPicks} is the queue of colours still to search for, one card per
 * colour to hand, in a "search for a white card, a blue card, ..." flow (Conflux) — after each
 * single-colour pick resolves the next colour begins its own search, and the library is shuffled
 * once when the queue empties.
 */
public record LibrarySearchFollowUp(boolean basicLandToHand, boolean cardToGraveyard,
                                    List<UUID> remainingEachPlayerBasicLandSearches,
                                    boolean eachPlayerSearchTapped,
                                    PendingOpponentExileChoice opponentExileChoice,
                                    UUID imprintSourcePermanentId,
                                    List<UUID> remainingEachPlayerCreatureToHandSearches,
                                    int eachPlayerCreatureToHandCount,
                                    List<UUID> remainingEachPlayerCreatureToBattlefieldSearches,
                                    SecondBoundedPick secondBoundedPick,
                                    List<String> remainingSameNamePicks,
                                    List<CardColor> remainingColorToHandPicks) {

    /**
     * State for the second of two bounded picks: the card {@code type} still to be offered and where
     * the unchosen looked-at cards go once it resolves ({@code restToGraveyard} true = graveyard,
     * false = bottom of the library).
     */
    public record SecondBoundedPick(CardType type, boolean restToGraveyard) {
    }

    public static final LibrarySearchFollowUp NONE =
            new LibrarySearchFollowUp(false, false, List.of(), false, null, null, List.of(), 0, List.of(), null, List.of(), List.of());

    public LibrarySearchFollowUp {
        remainingEachPlayerBasicLandSearches = List.copyOf(remainingEachPlayerBasicLandSearches);
        remainingEachPlayerCreatureToHandSearches = List.copyOf(remainingEachPlayerCreatureToHandSearches);
        remainingEachPlayerCreatureToBattlefieldSearches = List.copyOf(remainingEachPlayerCreatureToBattlefieldSearches);
        remainingSameNamePicks = List.copyOf(remainingSameNamePicks);
        remainingColorToHandPicks = List.copyOf(remainingColorToHandPicks);
    }

    public static LibrarySearchFollowUp forBasicLandToHand() {
        return new LibrarySearchFollowUp(true, false, List.of(), false, null, null, List.of(), 0, List.of(), null, List.of(), List.of());
    }

    public static LibrarySearchFollowUp forCardToGraveyard() {
        return new LibrarySearchFollowUp(false, true, List.of(), false, null, null, List.of(), 0, List.of(), null, List.of(), List.of());
    }

    public static LibrarySearchFollowUp forSecondBoundedPick(CardType type, boolean restToGraveyard) {
        return new LibrarySearchFollowUp(false, false, List.of(), false, null, null, List.of(), 0, List.of(),
                new SecondBoundedPick(type, restToGraveyard), List.of(), List.of());
    }

    public static LibrarySearchFollowUp eachPlayerBasicLand(List<UUID> remainingSearchers, boolean tapped) {
        return new LibrarySearchFollowUp(false, false, remainingSearchers, tapped, null, null, List.of(), 0, List.of(), null, List.of(), List.of());
    }

    public static LibrarySearchFollowUp eachPlayerCreaturesToHand(List<UUID> remainingSearchers, int count) {
        return new LibrarySearchFollowUp(false, false, List.of(), false, null, null, remainingSearchers, count, List.of(), null, List.of(), List.of());
    }

    public static LibrarySearchFollowUp eachPlayerCreatureToBattlefield(List<UUID> remainingSearchers) {
        return new LibrarySearchFollowUp(false, false, List.of(), false, null, null, List.of(), 0, remainingSearchers, null, List.of(), List.of());
    }

    public static LibrarySearchFollowUp opponentExile(PendingOpponentExileChoice choice) {
        return new LibrarySearchFollowUp(false, false, List.of(), false, choice, null, List.of(), 0, List.of(), null, List.of(), List.of());
    }

    public static LibrarySearchFollowUp imprint(UUID sourcePermanentId) {
        return new LibrarySearchFollowUp(false, false, List.of(), false, null, sourcePermanentId, List.of(), 0, List.of(), null, List.of(), List.of());
    }

    /** The queue of permanent names still to search for (Clarion Ultimatum), one entry per chosen permanent. */
    public static LibrarySearchFollowUp sameNamePicks(List<String> names) {
        return new LibrarySearchFollowUp(false, false, List.of(), false, null, null, List.of(), 0, List.of(), null, names, List.of());
    }

    /** The queue of colours still to search for, one card per colour to hand (Conflux). */
    public static LibrarySearchFollowUp colorToHandPicks(List<CardColor> colors) {
        return new LibrarySearchFollowUp(false, false, List.of(), false, null, null, List.of(), 0, List.of(), null, List.of(), colors);
    }

    /** The same follow-up with the each-player basic-land remainder advanced past the current searcher. */
    public LibrarySearchFollowUp withRemainingEachPlayerBasicLandSearches(List<UUID> remaining) {
        return new LibrarySearchFollowUp(basicLandToHand, cardToGraveyard, remaining,
                eachPlayerSearchTapped, opponentExileChoice, imprintSourcePermanentId,
                remainingEachPlayerCreatureToHandSearches, eachPlayerCreatureToHandCount,
                remainingEachPlayerCreatureToBattlefieldSearches, secondBoundedPick, remainingSameNamePicks,
                remainingColorToHandPicks);
    }

    /** The same follow-up with the each-player creature-to-hand remainder advanced past the current searcher. */
    public LibrarySearchFollowUp withRemainingEachPlayerCreatureToHandSearches(List<UUID> remaining) {
        return new LibrarySearchFollowUp(basicLandToHand, cardToGraveyard,
                remainingEachPlayerBasicLandSearches, eachPlayerSearchTapped, opponentExileChoice,
                imprintSourcePermanentId, remaining, eachPlayerCreatureToHandCount,
                remainingEachPlayerCreatureToBattlefieldSearches, secondBoundedPick, remainingSameNamePicks,
                remainingColorToHandPicks);
    }

    /** The same follow-up with the each-player creature-to-battlefield remainder advanced past the current searcher. */
    public LibrarySearchFollowUp withRemainingEachPlayerCreatureToBattlefieldSearches(List<UUID> remaining) {
        return new LibrarySearchFollowUp(basicLandToHand, cardToGraveyard,
                remainingEachPlayerBasicLandSearches, eachPlayerSearchTapped, opponentExileChoice,
                imprintSourcePermanentId, remainingEachPlayerCreatureToHandSearches,
                eachPlayerCreatureToHandCount, remaining, secondBoundedPick, remainingSameNamePicks,
                remainingColorToHandPicks);
    }

    /** The same follow-up with the same-name-pick queue advanced past the current name (Clarion Ultimatum). */
    public LibrarySearchFollowUp withRemainingSameNamePicks(List<String> remaining) {
        return new LibrarySearchFollowUp(basicLandToHand, cardToGraveyard,
                remainingEachPlayerBasicLandSearches, eachPlayerSearchTapped, opponentExileChoice,
                imprintSourcePermanentId, remainingEachPlayerCreatureToHandSearches,
                eachPlayerCreatureToHandCount, remainingEachPlayerCreatureToBattlefieldSearches,
                secondBoundedPick, remaining, remainingColorToHandPicks);
    }

    /** The same follow-up with the colour-to-hand queue advanced past the current colour (Conflux). */
    public LibrarySearchFollowUp withRemainingColorToHandPicks(List<CardColor> remaining) {
        return new LibrarySearchFollowUp(basicLandToHand, cardToGraveyard,
                remainingEachPlayerBasicLandSearches, eachPlayerSearchTapped, opponentExileChoice,
                imprintSourcePermanentId, remainingEachPlayerCreatureToHandSearches,
                eachPlayerCreatureToHandCount, remainingEachPlayerCreatureToBattlefieldSearches,
                secondBoundedPick, remainingSameNamePicks, remaining);
    }

    /** The same follow-up with the consumed basic-land-to-hand flag cleared. */
    public LibrarySearchFollowUp clearBasicLandToHand() {
        return new LibrarySearchFollowUp(false, cardToGraveyard, remainingEachPlayerBasicLandSearches,
                eachPlayerSearchTapped, opponentExileChoice, imprintSourcePermanentId,
                remainingEachPlayerCreatureToHandSearches, eachPlayerCreatureToHandCount,
                remainingEachPlayerCreatureToBattlefieldSearches, secondBoundedPick, remainingSameNamePicks,
                remainingColorToHandPicks);
    }

    /** The same follow-up with the consumed card-to-graveyard flag cleared. */
    public LibrarySearchFollowUp clearCardToGraveyard() {
        return new LibrarySearchFollowUp(basicLandToHand, false, remainingEachPlayerBasicLandSearches,
                eachPlayerSearchTapped, opponentExileChoice, imprintSourcePermanentId,
                remainingEachPlayerCreatureToHandSearches, eachPlayerCreatureToHandCount,
                remainingEachPlayerCreatureToBattlefieldSearches, secondBoundedPick, remainingSameNamePicks,
                remainingColorToHandPicks);
    }
}
