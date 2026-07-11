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
 * {@code imprintSourcePermanentId} receives the imprinted card at EXILE_IMPRINT completion.
 */
public record LibrarySearchFollowUp(boolean basicLandToHand, boolean cardToGraveyard,
                                    List<UUID> remainingEachPlayerBasicLandSearches,
                                    boolean eachPlayerSearchTapped,
                                    PendingOpponentExileChoice opponentExileChoice,
                                    UUID imprintSourcePermanentId,
                                    List<UUID> remainingEachPlayerCreatureToHandSearches,
                                    int eachPlayerCreatureToHandCount,
                                    List<UUID> remainingEachPlayerCreatureToBattlefieldSearches) {

    public static final LibrarySearchFollowUp NONE =
            new LibrarySearchFollowUp(false, false, List.of(), false, null, null, List.of(), 0, List.of());

    public LibrarySearchFollowUp {
        remainingEachPlayerBasicLandSearches = List.copyOf(remainingEachPlayerBasicLandSearches);
        remainingEachPlayerCreatureToHandSearches = List.copyOf(remainingEachPlayerCreatureToHandSearches);
        remainingEachPlayerCreatureToBattlefieldSearches = List.copyOf(remainingEachPlayerCreatureToBattlefieldSearches);
    }

    public static LibrarySearchFollowUp forBasicLandToHand() {
        return new LibrarySearchFollowUp(true, false, List.of(), false, null, null, List.of(), 0, List.of());
    }

    public static LibrarySearchFollowUp forCardToGraveyard() {
        return new LibrarySearchFollowUp(false, true, List.of(), false, null, null, List.of(), 0, List.of());
    }

    public static LibrarySearchFollowUp eachPlayerBasicLand(List<UUID> remainingSearchers, boolean tapped) {
        return new LibrarySearchFollowUp(false, false, remainingSearchers, tapped, null, null, List.of(), 0, List.of());
    }

    public static LibrarySearchFollowUp eachPlayerCreaturesToHand(List<UUID> remainingSearchers, int count) {
        return new LibrarySearchFollowUp(false, false, List.of(), false, null, null, remainingSearchers, count, List.of());
    }

    public static LibrarySearchFollowUp eachPlayerCreatureToBattlefield(List<UUID> remainingSearchers) {
        return new LibrarySearchFollowUp(false, false, List.of(), false, null, null, List.of(), 0, remainingSearchers);
    }

    public static LibrarySearchFollowUp opponentExile(PendingOpponentExileChoice choice) {
        return new LibrarySearchFollowUp(false, false, List.of(), false, choice, null, List.of(), 0, List.of());
    }

    public static LibrarySearchFollowUp imprint(UUID sourcePermanentId) {
        return new LibrarySearchFollowUp(false, false, List.of(), false, null, sourcePermanentId, List.of(), 0, List.of());
    }

    /** The same follow-up with the each-player basic-land remainder advanced past the current searcher. */
    public LibrarySearchFollowUp withRemainingEachPlayerBasicLandSearches(List<UUID> remaining) {
        return new LibrarySearchFollowUp(basicLandToHand, cardToGraveyard, remaining,
                eachPlayerSearchTapped, opponentExileChoice, imprintSourcePermanentId,
                remainingEachPlayerCreatureToHandSearches, eachPlayerCreatureToHandCount,
                remainingEachPlayerCreatureToBattlefieldSearches);
    }

    /** The same follow-up with the each-player creature-to-hand remainder advanced past the current searcher. */
    public LibrarySearchFollowUp withRemainingEachPlayerCreatureToHandSearches(List<UUID> remaining) {
        return new LibrarySearchFollowUp(basicLandToHand, cardToGraveyard,
                remainingEachPlayerBasicLandSearches, eachPlayerSearchTapped, opponentExileChoice,
                imprintSourcePermanentId, remaining, eachPlayerCreatureToHandCount,
                remainingEachPlayerCreatureToBattlefieldSearches);
    }

    /** The same follow-up with the each-player creature-to-battlefield remainder advanced past the current searcher. */
    public LibrarySearchFollowUp withRemainingEachPlayerCreatureToBattlefieldSearches(List<UUID> remaining) {
        return new LibrarySearchFollowUp(basicLandToHand, cardToGraveyard,
                remainingEachPlayerBasicLandSearches, eachPlayerSearchTapped, opponentExileChoice,
                imprintSourcePermanentId, remainingEachPlayerCreatureToHandSearches,
                eachPlayerCreatureToHandCount, remaining);
    }

    /** The same follow-up with the consumed basic-land-to-hand flag cleared. */
    public LibrarySearchFollowUp clearBasicLandToHand() {
        return new LibrarySearchFollowUp(false, cardToGraveyard, remainingEachPlayerBasicLandSearches,
                eachPlayerSearchTapped, opponentExileChoice, imprintSourcePermanentId,
                remainingEachPlayerCreatureToHandSearches, eachPlayerCreatureToHandCount,
                remainingEachPlayerCreatureToBattlefieldSearches);
    }

    /** The same follow-up with the consumed card-to-graveyard flag cleared. */
    public LibrarySearchFollowUp clearCardToGraveyard() {
        return new LibrarySearchFollowUp(basicLandToHand, false, remainingEachPlayerBasicLandSearches,
                eachPlayerSearchTapped, opponentExileChoice, imprintSourcePermanentId,
                remainingEachPlayerCreatureToHandSearches, eachPlayerCreatureToHandCount,
                remainingEachPlayerCreatureToBattlefieldSearches);
    }
}
