package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import java.util.List;
import java.util.UUID;

/**
 * Carry-over work attached to a {@link PendingInteraction.LibrarySearch} (via
 * {@link LibrarySearchParams#followUp()}) and performed (or continued) when the search
 * completes. Replaces the per-mechanic {@code pending*} fields {@code GameData} used to hold:
 * {@code basicLandToHand} begins the second Cultivate-style pick (basic land to hand);
 * {@code cardToGraveyard} begins the second Final Parting / Jarad's Orders pick (card to graveyard);
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
 * name and put it onto the battlefield" flow (Clarion Ultimatum tapped, Doubling Chant untapped
 * and creature-only) — after each single-name pick resolves the next name in the queue begins its
 * own search;
 * {@code remainingToHandPicks} is the queue of descriptors still to search for, one card per
 * descriptor to hand, in a "search for a white card, a blue card, ..." (Conflux), "search for an
 * Island card, a Swamp card, and a Mountain card" (Gem of Becoming), or "search for a card named
 * Forest, a card named Brambleweft Behemoth, ..." (Nissa's Encouragement, library remainder after
 * graveyard auto-takes) flow — after each single-pick search resolves the next descriptor begins
 * its own search, and the library is shuffled once when the queue empties;
 * {@code remainingInstantManaValueToHandPicks} is the queue of exact mana values still to search
 * for, one instant card per value to hand, in a "search for an instant with mana value 3, then 2,
 * then 1" flow (Firemind's Foresight) — after each pick resolves the next value begins its own
 * search, and the library is shuffled once when the queue empties;
 * {@code naturalBalance} is the APNAP remainder of Natural Balance's per-player "up to X basic
 * lands onto the battlefield" searches plus the forced land sacrifices to perform once every
 * search has resolved.
 */
public record LibrarySearchFollowUp(boolean basicLandToHand, CardToGraveyardPick cardToGraveyard,
                                    List<UUID> remainingEachPlayerBasicLandSearches,
                                    boolean eachPlayerSearchTapped,
                                    PendingOpponentExileChoice opponentExileChoice,
                                    UUID imprintSourcePermanentId,
                                    List<UUID> remainingEachPlayerCreatureToHandSearches,
                                    int eachPlayerCreatureToHandCount,
                                    List<UUID> remainingEachPlayerCreatureToBattlefieldSearches,
                                    SecondBoundedPick secondBoundedPick,
                                    SameNamePickQueue remainingSameNamePicks,
                                    List<ToHandPick> remainingToHandPicks,
                                    List<Integer> remainingInstantManaValueToHandPicks,
                                    NaturalBalanceQueue naturalBalance) {

    /**
     * One queued "search your library for a &lt;descriptor&gt; card, reveal it, put it into your hand"
     * pick. Exactly one of {@code color} (Conflux), {@code subtype} (Gem of Becoming), or
     * {@code cardName} (Nissa's Encouragement) is set; it selects the library cards offered for that
     * pick and names it in the prompt.
     */
    public record ToHandPick(CardColor color, CardSubtype subtype, String cardName) {

        public static ToHandPick ofColor(CardColor color) {
            return new ToHandPick(color, null, null);
        }

        public static ToHandPick ofSubtype(CardSubtype subtype) {
            return new ToHandPick(null, subtype, null);
        }

        public static ToHandPick ofName(String cardName) {
            return new ToHandPick(null, null, cardName);
        }

        /**
         * Colour/subtype descriptor spliced into "a &lt;descriptor&gt; card" prompts
         * ("blue", "Island"). Named picks use {@link #cardName()} directly instead.
         */
        public String describe() {
            if (cardName != null) {
                return cardName;
            }
            return color != null ? color.name().toLowerCase() : subtype.getDisplayName();
        }
    }

    /**
     * Second pick of a hand-then-graveyard library search: optional {@code filter} (null =
     * unrestricted), whether the pick may fail to find, and whether the chosen card is revealed.
     */
    public record CardToGraveyardPick(CardPredicate filter, boolean canFailToFind, boolean reveals) {
        public static CardToGraveyardPick unrestricted() {
            return new CardToGraveyardPick(null, false, false);
        }
    }

    /**
     * State for the second of two bounded picks: the card {@code type} still to be offered and where
     * the unchosen looked-at cards go once it resolves ({@code restToGraveyard} true = graveyard,
     * false = bottom of the library).
     */
    public record SecondBoundedPick(CardType type, boolean restToGraveyard) {
    }

    /**
     * A "for each of these permanents, you may search your library for a card with the same name and
     * put it onto the battlefield" queue: the {@code names} still to be searched for, whether only
     * creature cards qualify ({@code creatureOnly}, Doubling Chant) and where the found card goes.
     */
    public record SameNamePickQueue(List<String> names, boolean creatureOnly,
                                    LibrarySearchDestination destination) {

        public SameNamePickQueue {
            names = List.copyOf(names);
        }

        public SameNamePickQueue withNames(List<String> remaining) {
            return new SameNamePickQueue(remaining, creatureOnly, destination);
        }
    }

    /** One player's "may search for up to {@code count} basic land cards" pick (Natural Balance). */
    public record BasicLandsPick(UUID playerId, int count) {
    }

    /**
     * Natural Balance's carry-over: the APNAP-ordered basic-land picks still to be offered, and the
     * forced land sacrifices to run once the last pick has resolved.
     */
    public record NaturalBalanceQueue(List<BasicLandsPick> remainingPicks,
                                      List<PendingForcedSacrifice> sacrifices) {

        public NaturalBalanceQueue {
            remainingPicks = List.copyOf(remainingPicks);
            sacrifices = List.copyOf(sacrifices);
        }

        public NaturalBalanceQueue withRemainingPicks(List<BasicLandsPick> remaining) {
            return new NaturalBalanceQueue(remaining, sacrifices);
        }
    }

    public static final LibrarySearchFollowUp NONE =
            new LibrarySearchFollowUp(false, null, List.of(), false, null, null, List.of(), 0, List.of(), null, null,
                    List.of(), null, null);

    public LibrarySearchFollowUp {
        remainingEachPlayerBasicLandSearches = List.copyOf(remainingEachPlayerBasicLandSearches);
        remainingEachPlayerCreatureToHandSearches = List.copyOf(remainingEachPlayerCreatureToHandSearches);
        remainingEachPlayerCreatureToBattlefieldSearches = List.copyOf(remainingEachPlayerCreatureToBattlefieldSearches);
        remainingToHandPicks = List.copyOf(remainingToHandPicks);
        remainingInstantManaValueToHandPicks = remainingInstantManaValueToHandPicks == null
                ? null
                : List.copyOf(remainingInstantManaValueToHandPicks);
    }

    public static LibrarySearchFollowUp forBasicLandToHand() {
        return new LibrarySearchFollowUp(true, null, List.of(), false, null, null, List.of(), 0, List.of(), null, null,
                List.of(), null, null);
    }

    /** Final Parting: unrestricted mandatory second pick to graveyard. */
    public static LibrarySearchFollowUp forCardToGraveyard() {
        return forCardToGraveyard(CardToGraveyardPick.unrestricted());
    }

    /** Second pick to graveyard with the same filter / fail / reveal settings as the first pick. */
    public static LibrarySearchFollowUp forCardToGraveyard(CardToGraveyardPick pick) {
        return new LibrarySearchFollowUp(false, pick, List.of(), false, null, null, List.of(), 0, List.of(), null, null,
                List.of(), null, null);
    }

    public static LibrarySearchFollowUp forSecondBoundedPick(CardType type, boolean restToGraveyard) {
        return new LibrarySearchFollowUp(false, null, List.of(), false, null, null, List.of(), 0, List.of(),
                new SecondBoundedPick(type, restToGraveyard), null, List.of(), null, null);
    }

    public static LibrarySearchFollowUp eachPlayerBasicLand(List<UUID> remainingSearchers, boolean tapped) {
        return new LibrarySearchFollowUp(false, null, remainingSearchers, tapped, null, null, List.of(), 0, List.of(),
                null, null, List.of(), null, null);
    }

    public static LibrarySearchFollowUp eachPlayerCreaturesToHand(List<UUID> remainingSearchers, int count) {
        return new LibrarySearchFollowUp(false, null, List.of(), false, null, null, remainingSearchers, count,
                List.of(), null, null, List.of(), null, null);
    }

    public static LibrarySearchFollowUp eachPlayerCreatureToBattlefield(List<UUID> remainingSearchers) {
        return new LibrarySearchFollowUp(false, null, List.of(), false, null, null, List.of(), 0, remainingSearchers,
                null, null, List.of(), null, null);
    }

    public static LibrarySearchFollowUp opponentExile(PendingOpponentExileChoice choice) {
        return new LibrarySearchFollowUp(false, null, List.of(), false, choice, null, List.of(), 0, List.of(), null,
                null, List.of(), null, null);
    }

    public static LibrarySearchFollowUp imprint(UUID sourcePermanentId) {
        return new LibrarySearchFollowUp(false, null, List.of(), false, null, sourcePermanentId, List.of(), 0,
                List.of(), null, null, List.of(), null, null);
    }

    /** The queue of permanent names still to search for, one entry per permanent (Clarion Ultimatum, Doubling Chant). */
    public static LibrarySearchFollowUp sameNamePicks(List<String> names, boolean creatureOnly,
                                                      LibrarySearchDestination destination) {
        return new LibrarySearchFollowUp(false, null, List.of(), false, null, null, List.of(), 0, List.of(), null,
                new SameNamePickQueue(names, creatureOnly, destination), List.of(), null, null);
    }

    /** The queue of colours still to search for, one card per colour to hand (Conflux). */
    public static LibrarySearchFollowUp colorToHandPicks(List<CardColor> colors) {
        return toHandPicks(colors.stream().map(ToHandPick::ofColor).toList());
    }

    /** The queue of subtypes still to search for, one card per subtype to hand (Gem of Becoming). */
    public static LibrarySearchFollowUp subtypeToHandPicks(List<CardSubtype> subtypes) {
        return toHandPicks(subtypes.stream().map(ToHandPick::ofSubtype).toList());
    }

    /**
     * The queue of exact card names still to search for, one card per name to hand
     * (Nissa's Encouragement library remainder).
     */
    public static LibrarySearchFollowUp namedToHandPicks(List<String> names) {
        return toHandPicks(names.stream().map(ToHandPick::ofName).toList());
    }

    private static LibrarySearchFollowUp toHandPicks(List<ToHandPick> picks) {
        return new LibrarySearchFollowUp(false, null, List.of(), false, null, null, List.of(), 0, List.of(), null, null,
                picks, null, null);
    }

    /**
     * The queue of exact mana values still to search for, one instant card per value to hand
     * (Firemind's Foresight). {@code null} on the follow-up means this mechanic is absent;
     * an empty list means every value has already been searched (shuffle once).
     */
    public static LibrarySearchFollowUp instantManaValueToHandPicks(List<Integer> manaValues) {
        return new LibrarySearchFollowUp(false, null, List.of(), false, null, null, List.of(), 0, List.of(), null, null,
                List.of(), List.copyOf(manaValues), null);
    }

    /** Natural Balance's remaining per-player basic-land picks plus the sacrifices that follow them. */
    public static LibrarySearchFollowUp naturalBalance(List<BasicLandsPick> remainingPicks,
                                                       List<PendingForcedSacrifice> sacrifices) {
        return new LibrarySearchFollowUp(false, null, List.of(), false, null, null, List.of(), 0, List.of(), null,
                null, List.of(), null, new NaturalBalanceQueue(remainingPicks, sacrifices));
    }

    /** The same follow-up with the each-player basic-land remainder advanced past the current searcher. */
    public LibrarySearchFollowUp withRemainingEachPlayerBasicLandSearches(List<UUID> remaining) {
        return new LibrarySearchFollowUp(basicLandToHand, cardToGraveyard, remaining,
                eachPlayerSearchTapped, opponentExileChoice, imprintSourcePermanentId,
                remainingEachPlayerCreatureToHandSearches, eachPlayerCreatureToHandCount,
                remainingEachPlayerCreatureToBattlefieldSearches, secondBoundedPick, remainingSameNamePicks,
                remainingToHandPicks, remainingInstantManaValueToHandPicks, naturalBalance);
    }

    /** The same follow-up with the each-player creature-to-hand remainder advanced past the current searcher. */
    public LibrarySearchFollowUp withRemainingEachPlayerCreatureToHandSearches(List<UUID> remaining) {
        return new LibrarySearchFollowUp(basicLandToHand, cardToGraveyard,
                remainingEachPlayerBasicLandSearches, eachPlayerSearchTapped, opponentExileChoice,
                imprintSourcePermanentId, remaining, eachPlayerCreatureToHandCount,
                remainingEachPlayerCreatureToBattlefieldSearches, secondBoundedPick, remainingSameNamePicks,
                remainingToHandPicks, remainingInstantManaValueToHandPicks, naturalBalance);
    }

    /** The same follow-up with the each-player creature-to-battlefield remainder advanced past the current searcher. */
    public LibrarySearchFollowUp withRemainingEachPlayerCreatureToBattlefieldSearches(List<UUID> remaining) {
        return new LibrarySearchFollowUp(basicLandToHand, cardToGraveyard,
                remainingEachPlayerBasicLandSearches, eachPlayerSearchTapped, opponentExileChoice,
                imprintSourcePermanentId, remainingEachPlayerCreatureToHandSearches,
                eachPlayerCreatureToHandCount, remaining, secondBoundedPick, remainingSameNamePicks,
                remainingToHandPicks, remainingInstantManaValueToHandPicks, naturalBalance);
    }

    /** The same follow-up with the same-name-pick queue advanced past the current name. */
    public LibrarySearchFollowUp withRemainingSameNamePicks(SameNamePickQueue remaining) {
        return new LibrarySearchFollowUp(basicLandToHand, cardToGraveyard,
                remainingEachPlayerBasicLandSearches, eachPlayerSearchTapped, opponentExileChoice,
                imprintSourcePermanentId, remainingEachPlayerCreatureToHandSearches,
                eachPlayerCreatureToHandCount, remainingEachPlayerCreatureToBattlefieldSearches,
                secondBoundedPick, remaining, remainingToHandPicks, remainingInstantManaValueToHandPicks,
                naturalBalance);
    }

    /** The same follow-up with the to-hand pick queue advanced past the current pick. */
    public LibrarySearchFollowUp withRemainingToHandPicks(List<ToHandPick> remaining) {
        return new LibrarySearchFollowUp(basicLandToHand, cardToGraveyard,
                remainingEachPlayerBasicLandSearches, eachPlayerSearchTapped, opponentExileChoice,
                imprintSourcePermanentId, remainingEachPlayerCreatureToHandSearches,
                eachPlayerCreatureToHandCount, remainingEachPlayerCreatureToBattlefieldSearches,
                secondBoundedPick, remainingSameNamePicks, remaining,
                remainingInstantManaValueToHandPicks, naturalBalance);
    }

    /**
     * The same follow-up with the instant-mana-value-to-hand queue advanced past the current value
     * (Firemind's Foresight).
     */
    public LibrarySearchFollowUp withRemainingInstantManaValueToHandPicks(List<Integer> remaining) {
        return new LibrarySearchFollowUp(basicLandToHand, cardToGraveyard,
                remainingEachPlayerBasicLandSearches, eachPlayerSearchTapped, opponentExileChoice,
                imprintSourcePermanentId, remainingEachPlayerCreatureToHandSearches,
                eachPlayerCreatureToHandCount, remainingEachPlayerCreatureToBattlefieldSearches,
                secondBoundedPick, remainingSameNamePicks, remainingToHandPicks, remaining, naturalBalance);
    }

    /** The same follow-up with Natural Balance's pick queue advanced past the current searcher. */
    public LibrarySearchFollowUp withNaturalBalance(NaturalBalanceQueue queue) {
        return new LibrarySearchFollowUp(basicLandToHand, cardToGraveyard,
                remainingEachPlayerBasicLandSearches, eachPlayerSearchTapped, opponentExileChoice,
                imprintSourcePermanentId, remainingEachPlayerCreatureToHandSearches,
                eachPlayerCreatureToHandCount, remainingEachPlayerCreatureToBattlefieldSearches,
                secondBoundedPick, remainingSameNamePicks, remainingToHandPicks,
                remainingInstantManaValueToHandPicks, queue);
    }

    /** The same follow-up with the consumed basic-land-to-hand flag cleared. */
    public LibrarySearchFollowUp clearBasicLandToHand() {
        return new LibrarySearchFollowUp(false, cardToGraveyard, remainingEachPlayerBasicLandSearches,
                eachPlayerSearchTapped, opponentExileChoice, imprintSourcePermanentId,
                remainingEachPlayerCreatureToHandSearches, eachPlayerCreatureToHandCount,
                remainingEachPlayerCreatureToBattlefieldSearches, secondBoundedPick, remainingSameNamePicks,
                remainingToHandPicks, remainingInstantManaValueToHandPicks, naturalBalance);
    }

    /** The same follow-up with the consumed card-to-graveyard pick cleared. */
    public LibrarySearchFollowUp clearCardToGraveyard() {
        return new LibrarySearchFollowUp(basicLandToHand, null, remainingEachPlayerBasicLandSearches,
                eachPlayerSearchTapped, opponentExileChoice, imprintSourcePermanentId,
                remainingEachPlayerCreatureToHandSearches, eachPlayerCreatureToHandCount,
                remainingEachPlayerCreatureToBattlefieldSearches, secondBoundedPick, remainingSameNamePicks,
                remainingToHandPicks, remainingInstantManaValueToHandPicks, naturalBalance);
    }
}
