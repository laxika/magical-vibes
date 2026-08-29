package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import java.util.List;
import java.util.UUID;

/**
 * Carry-over work attached to a {@link PendingInteraction.LibrarySearch} (via
 * {@link LibrarySearchParams#followUp()}) and performed (or continued) when the search
 * completes. Replaces the per-mechanic {@code pending*} fields {@code GameData} used to hold:
 * {@code basicLandToHand} begins the second Cultivate-style pick (one or more basic lands to hand,
 * optionally restricted to a land subtype);
 * {@code cardToGraveyard} begins the second Final Parting / Jarad's Orders pick (card to graveyard);
 * the each-player basic-land pair is the APNAP remainder of an "each player searches for a basic
 * land" flow (Field of Ruin, Old-Growth Dryads), advanced after each player's search resolves;
 * the each-player to-hand triple is the APNAP remainder of an "each player may search for up to N
 * cards to hand" flow (Weird Harvest, Noble Benefactor), carrying the shared per-player count and
 * whether the search is restricted to creature cards (and therefore reveals them);
 * the each-player creature-to-battlefield list is the APNAP remainder of an "each opponent may
 * search for a creature card to battlefield" flow (Boldwyr Heavyweights);
 * {@code remainingTargetPlayerTopSearches} is the remainder of a search where each of several
 * targeted players searches their own library for a card and puts it on top (Scheming Symmetry);
 * {@code opponentExileChoice} prompts the opponent after the Distant Memories exile;
 * {@code imprintSourcePermanentId} receives the imprinted card when the face-down exile completes;
     * {@code secondBoundedPick} begins the next bounded pick (may reveal a card of its type or
     * subtype from the same looked-at cards to hand, then dispose the rest) after the prior pick
     * resolves — Gift of the Gargantuan, Benefaction of Rhonas, and Kaalia, Zenith Seeker;
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
 * {@code basicLandSearchQueue} is the APNAP remainder of a per-player "each player may search for
 * up to X basic land cards and put them onto the battlefield" flow (Natural Balance, Veteran
 * Explorer) plus the forced land sacrifices to perform once every search has resolved (Natural
 * Balance only; the list is empty when the flow has no sacrifice half).
 * {@code grimReminderSearch} carries the selected card name's life-loss amount for Grim Reminder's
 * reveal-only search.
 * {@code remainingEachPlayerLandToBattlefieldSearches} is the APNAP remainder of an opponent land
 * search flow (Hired Giant).
 */
public record LibrarySearchFollowUp(BasicLandToHandPick basicLandToHand, CardToGraveyardPick cardToGraveyard,
                                    List<UUID> remainingEachPlayerBasicLandSearches,
                                    boolean eachPlayerSearchTapped,
                                    PendingOpponentExileChoice opponentExileChoice,
                                    UUID imprintSourcePermanentId,
                                    List<UUID> remainingEachPlayerToHandSearches,
                                    int eachPlayerToHandCount,
                                    boolean eachPlayerToHandCreatureOnly,
                                    List<UUID> remainingEachPlayerCreatureToBattlefieldSearches,
                                    List<UUID> remainingTargetPlayerTopSearches,
                                    SecondBoundedPick secondBoundedPick,
                                    SameNamePickQueue remainingSameNamePicks,
                                    List<ToHandPick> remainingToHandPicks,
                                    List<Integer> remainingInstantManaValueToHandPicks,
                                    BasicLandSearchQueue basicLandSearchQueue,
                                    GrimReminderSearch grimReminderSearch,
                                    List<UUID> remainingEachPlayerLandToBattlefieldSearches,
                                    SelectedCardFollowUp selectedCardFollowUp) {

    public record SelectedCardFollowUp(CardPredicate predicate, CardEffect effect) {
    }

    /** Completion data for Grim Reminder's reveal-only library search. */
    public record GrimReminderSearch(int lifeLoss) {
    }

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
     * State for the second of two bounded picks: the card {@code type}, {@code subtype}, or custom
     * {@code predicate} still to be offered and where the unchosen looked-at cards go once it
     * resolves ({@code restToGraveyard} true = graveyard, false = bottom of the library).
     */
    public record SecondBoundedPick(CardType type, boolean restToGraveyard, CardSubtype subtype,
                                    List<CardSubtype> remainingSubtypes, boolean randomRest,
                                    List<CardType> remainingTypes,
                                    LibrarySearchDestination destination,
                                    CardPredicate predicate,
                                    String prompt) {

        public SecondBoundedPick(CardType type, boolean restToGraveyard, CardSubtype subtype,
                                 List<CardSubtype> remainingSubtypes, boolean randomRest,
                                 List<CardType> remainingTypes, LibrarySearchDestination destination) {
            this(type, restToGraveyard, subtype, remainingSubtypes, randomRest, remainingTypes,
                    destination, null, null);
        }

        public SecondBoundedPick(CardType type, boolean restToGraveyard, CardSubtype subtype,
                                 List<CardSubtype> remainingSubtypes, boolean randomRest,
                                 List<CardType> remainingTypes) {
            this(type, restToGraveyard, subtype, remainingSubtypes, randomRest, remainingTypes,
                    LibrarySearchDestination.HAND, null, null);
        }

        public SecondBoundedPick {
            remainingSubtypes = List.copyOf(remainingSubtypes);
            remainingTypes = List.copyOf(remainingTypes);
        }

        public SecondBoundedPick(CardType type, boolean restToGraveyard) {
            this(type, restToGraveyard, null, List.of(), false, List.of());
        }

        public static SecondBoundedPick subtype(CardSubtype subtype, List<CardSubtype> remaining,
                                                boolean randomRest) {
            return subtype(subtype, remaining, randomRest, LibrarySearchDestination.HAND);
        }

        public static SecondBoundedPick subtype(CardSubtype subtype, List<CardSubtype> remaining,
                                                boolean randomRest, LibrarySearchDestination destination) {
            return new SecondBoundedPick(null, false, subtype, remaining, randomRest, List.of(), destination);
        }

        /** The next card-type pick in a dynamic one-per-type flow. */
        public static SecondBoundedPick cardType(CardType type, List<CardType> remaining,
                                                 boolean randomRest) {
            return cardType(type, remaining, randomRest, LibrarySearchDestination.HAND);
        }

        public static SecondBoundedPick cardType(CardType type, List<CardType> remaining,
                                                 boolean randomRest, LibrarySearchDestination destination) {
            return new SecondBoundedPick(type, false, null, List.of(), randomRest, remaining, destination);
        }

        public static SecondBoundedPick terminal(boolean randomRest) {
            return terminal(randomRest, LibrarySearchDestination.HAND);
        }

        public static SecondBoundedPick terminal(boolean randomRest, LibrarySearchDestination destination) {
            return new SecondBoundedPick(null, false, null, List.of(), randomRest, List.of(), destination);
        }

        public static SecondBoundedPick predicate(CardPredicate predicate, String prompt,
                                                  boolean randomRest,
                                                  LibrarySearchDestination destination) {
            return new SecondBoundedPick(null, false, null, List.of(), randomRest, List.of(),
                    destination, predicate, prompt);
        }
    }

    /**
     * A "for each of these permanents, you may search your library for a card with the same name and
     * put it onto the battlefield" queue: the {@code names} still to be searched for, whether only
     * creature cards qualify ({@code creatureOnly}, Doubling Chant), whether each search is optional,
     * and where the found card goes.
     */
    public record SameNamePickQueue(List<String> names, boolean creatureOnly,
                                    LibrarySearchDestination destination,
                                    UUID libraryOwnerId, UUID battlefieldControllerId,
                                    boolean optional) {

        public SameNamePickQueue(List<String> names, boolean creatureOnly,
                                 LibrarySearchDestination destination) {
            this(names, creatureOnly, destination, null, null, true);
        }

        public SameNamePickQueue {
            names = List.copyOf(names);
        }

        public SameNamePickQueue withNames(List<String> remaining) {
            return new SameNamePickQueue(remaining, creatureOnly, destination,
                    libraryOwnerId, battlefieldControllerId, optional);
        }
    }

    /**
     * The remaining "search your library for a land card and put it into your hand" picks of a
     * Cultivate-style search: how many picks are still owed, the land {@code subtype} the found
     * cards must have, and whether they must be basic lands.
     */
    public record BasicLandToHandPick(int count, CardSubtype subtype, boolean basicOnly) {

        public BasicLandToHandPick(int count, CardSubtype subtype) {
            this(count, subtype, true);
        }

        /** The same pick with one card taken off the remaining count (null once none are left). */
        public BasicLandToHandPick decremented() {
            return count <= 1 ? null : new BasicLandToHandPick(count - 1, subtype, basicOnly);
        }
    }

    /** One player's "may search for up to {@code count} basic land cards" pick. */
    public record BasicLandsPick(UUID playerId, int count, boolean enterTapped) {

        public BasicLandsPick(UUID playerId, int count) {
            this(playerId, count, false);
        }
    }

    /**
     * The carry-over of an each-player basic-land search: the APNAP-ordered picks still to be
     * offered, and the forced land sacrifices to run once the last pick has resolved (empty unless
     * the flow has a sacrifice half, as Natural Balance does).
     */
    public record BasicLandSearchQueue(List<BasicLandsPick> remainingPicks,
                                      List<PendingForcedSacrifice> sacrifices) {

        public BasicLandSearchQueue {
            remainingPicks = List.copyOf(remainingPicks);
            sacrifices = List.copyOf(sacrifices);
        }

        public BasicLandSearchQueue withRemainingPicks(List<BasicLandsPick> remaining) {
            return new BasicLandSearchQueue(remaining, sacrifices);
        }
    }

    public static final LibrarySearchFollowUp NONE =
            new LibrarySearchFollowUp(null, null, List.of(), false, null, null, List.of(), 0, false, List.of(), null, null,
                    List.of(), null, null, null);

    public LibrarySearchFollowUp {
        remainingEachPlayerBasicLandSearches = List.copyOf(remainingEachPlayerBasicLandSearches);
        remainingEachPlayerToHandSearches = List.copyOf(remainingEachPlayerToHandSearches);
        remainingEachPlayerCreatureToBattlefieldSearches = List.copyOf(remainingEachPlayerCreatureToBattlefieldSearches);
        remainingTargetPlayerTopSearches = List.copyOf(remainingTargetPlayerTopSearches);
        remainingToHandPicks = List.copyOf(remainingToHandPicks);
        remainingInstantManaValueToHandPicks = remainingInstantManaValueToHandPicks == null
                ? null
                : List.copyOf(remainingInstantManaValueToHandPicks);
        remainingEachPlayerLandToBattlefieldSearches = List.copyOf(remainingEachPlayerLandToBattlefieldSearches);
    }

    public LibrarySearchFollowUp(BasicLandToHandPick basicLandToHand, CardToGraveyardPick cardToGraveyard,
                                 List<UUID> remainingEachPlayerBasicLandSearches,
                                 boolean eachPlayerSearchTapped,
                                 PendingOpponentExileChoice opponentExileChoice,
                                 UUID imprintSourcePermanentId,
                                 List<UUID> remainingEachPlayerToHandSearches,
                                 int eachPlayerToHandCount,
                                 boolean eachPlayerToHandCreatureOnly,
                                 List<UUID> remainingEachPlayerCreatureToBattlefieldSearches,
                                 List<UUID> remainingTargetPlayerTopSearches,
                                 SecondBoundedPick secondBoundedPick,
                                 SameNamePickQueue remainingSameNamePicks,
                                 List<ToHandPick> remainingToHandPicks,
                                 List<Integer> remainingInstantManaValueToHandPicks,
                                 BasicLandSearchQueue basicLandSearchQueue,
                                 GrimReminderSearch grimReminderSearch,
                                 List<UUID> remainingEachPlayerLandToBattlefieldSearches) {
        this(basicLandToHand, cardToGraveyard, remainingEachPlayerBasicLandSearches,
                eachPlayerSearchTapped, opponentExileChoice, imprintSourcePermanentId,
                remainingEachPlayerToHandSearches, eachPlayerToHandCount, eachPlayerToHandCreatureOnly,
                remainingEachPlayerCreatureToBattlefieldSearches, remainingTargetPlayerTopSearches,
                secondBoundedPick, remainingSameNamePicks, remainingToHandPicks,
                remainingInstantManaValueToHandPicks, basicLandSearchQueue, grimReminderSearch,
                remainingEachPlayerLandToBattlefieldSearches, null);
    }

    /** Backward-compatible constructor for follow-ups that do not use targeted top searches. */
    public LibrarySearchFollowUp(BasicLandToHandPick basicLandToHand, CardToGraveyardPick cardToGraveyard,
                                 List<UUID> remainingEachPlayerBasicLandSearches,
                                 boolean eachPlayerSearchTapped,
                                 PendingOpponentExileChoice opponentExileChoice,
                                 UUID imprintSourcePermanentId,
                                 List<UUID> remainingEachPlayerToHandSearches,
                                 int eachPlayerToHandCount,
                                 boolean eachPlayerToHandCreatureOnly,
                                 List<UUID> remainingEachPlayerCreatureToBattlefieldSearches,
                                 SecondBoundedPick secondBoundedPick,
                                 SameNamePickQueue remainingSameNamePicks,
                                 List<ToHandPick> remainingToHandPicks,
                                 List<Integer> remainingInstantManaValueToHandPicks,
                                 BasicLandSearchQueue basicLandSearchQueue,
                                 GrimReminderSearch grimReminderSearch) {
        this(basicLandToHand, cardToGraveyard, remainingEachPlayerBasicLandSearches,
                eachPlayerSearchTapped, opponentExileChoice, imprintSourcePermanentId,
                remainingEachPlayerToHandSearches, eachPlayerToHandCount, eachPlayerToHandCreatureOnly,
                remainingEachPlayerCreatureToBattlefieldSearches, List.of(), secondBoundedPick,
                remainingSameNamePicks, remainingToHandPicks, remainingInstantManaValueToHandPicks,
                basicLandSearchQueue, grimReminderSearch, List.of());
    }

    /** Backward-compatible constructor for the pre-Hired Giant follow-up shape. */
    public LibrarySearchFollowUp(BasicLandToHandPick basicLandToHand, CardToGraveyardPick cardToGraveyard,
                                 List<UUID> remainingEachPlayerBasicLandSearches,
                                 boolean eachPlayerSearchTapped,
                                 PendingOpponentExileChoice opponentExileChoice,
                                 UUID imprintSourcePermanentId,
                                 List<UUID> remainingEachPlayerToHandSearches,
                                 int eachPlayerToHandCount,
                                 boolean eachPlayerToHandCreatureOnly,
                                 List<UUID> remainingEachPlayerCreatureToBattlefieldSearches,
                                 List<UUID> remainingTargetPlayerTopSearches,
                                 SecondBoundedPick secondBoundedPick,
                                 SameNamePickQueue remainingSameNamePicks,
                                 List<ToHandPick> remainingToHandPicks,
                                 List<Integer> remainingInstantManaValueToHandPicks,
                                 BasicLandSearchQueue basicLandSearchQueue,
                                 GrimReminderSearch grimReminderSearch) {
        this(basicLandToHand, cardToGraveyard, remainingEachPlayerBasicLandSearches,
                eachPlayerSearchTapped, opponentExileChoice, imprintSourcePermanentId,
                remainingEachPlayerToHandSearches, eachPlayerToHandCount, eachPlayerToHandCreatureOnly,
                remainingEachPlayerCreatureToBattlefieldSearches, remainingTargetPlayerTopSearches,
                secondBoundedPick, remainingSameNamePicks, remainingToHandPicks,
                remainingInstantManaValueToHandPicks, basicLandSearchQueue, grimReminderSearch, List.of());
    }

    public static LibrarySearchFollowUp forBasicLandToHand() {
        return forBasicLandToHand(1, null);
    }

    public static LibrarySearchFollowUp forSelectedCard(CardPredicate predicate, CardEffect effect) {
        return new LibrarySearchFollowUp(null, null, List.of(), false, null, null, List.of(), 0,
                false, List.of(), List.of(), null, null, List.of(), null, null, null, List.of(),
                new SelectedCardFollowUp(predicate, effect));
    }

    /** Runs a selected-card follow-up before putting the unchosen bounded-pick cards back randomly. */
    public static LibrarySearchFollowUp forSelectedCardWithRandomRest(
            CardPredicate predicate, CardEffect effect) {
        return new LibrarySearchFollowUp(null, null, List.of(), false, null, null, List.of(), 0,
                false, List.of(), List.of(), SecondBoundedPick.terminal(true), null, List.of(), null,
                null, null, List.of(), new SelectedCardFollowUp(predicate, effect));
    }

    /**
     * {@code count} further basic land cards to hand, restricted to {@code subtype} when non-null
     * (Nissa's Pilgrimage searches for basic Forest cards only).
     */
    public static LibrarySearchFollowUp forBasicLandToHand(int count, CardSubtype subtype) {
        return new LibrarySearchFollowUp(new BasicLandToHandPick(count, subtype), null, List.of(), false, null, null, List.of(), 0, false, List.of(), null, null,
                List.of(), null, null, null);
    }

    /** Further land-subtype cards to hand, including nonbasic cards with that subtype. */
    public static LibrarySearchFollowUp forLandSubtypeToHand(int count, CardSubtype subtype) {
        return new LibrarySearchFollowUp(new BasicLandToHandPick(count, subtype, false), null, List.of(), false, null, null, List.of(), 0, false, List.of(), null, null,
                List.of(), null, null, null);
    }

    /** Final Parting: unrestricted mandatory second pick to graveyard. */
    public static LibrarySearchFollowUp forCardToGraveyard() {
        return forCardToGraveyard(CardToGraveyardPick.unrestricted());
    }

    /** Second pick to graveyard with the same filter / fail / reveal settings as the first pick. */
    public static LibrarySearchFollowUp forCardToGraveyard(CardToGraveyardPick pick) {
        return new LibrarySearchFollowUp(null, pick, List.of(), false, null, null, List.of(), 0, false, List.of(), null, null,
                List.of(), null, null, null);
    }

    public static LibrarySearchFollowUp forSecondBoundedPick(CardType type, boolean restToGraveyard) {
        return forSecondBoundedPick(type, restToGraveyard, LibrarySearchDestination.HAND);
    }

    public static LibrarySearchFollowUp forSecondBoundedPick(CardType type, boolean restToGraveyard,
                                                              LibrarySearchDestination destination) {
        return forSecondBoundedPick(type, restToGraveyard, false, destination);
    }

    public static LibrarySearchFollowUp forSecondBoundedPick(CardPredicate predicate, String prompt,
                                                              boolean randomRest,
                                                              LibrarySearchDestination destination) {
        return forBoundedPick(SecondBoundedPick.predicate(
                predicate, prompt, randomRest, destination));
    }

    public static LibrarySearchFollowUp forSecondBoundedPick(CardType type, boolean restToGraveyard,
                                                              boolean randomRest,
                                                              LibrarySearchDestination destination) {
        return new LibrarySearchFollowUp(null, null, List.of(), false, null, null, List.of(), 0, false, List.of(),
                new SecondBoundedPick(type, restToGraveyard, null, List.of(), randomRest, List.of(), destination),
                null, List.of(), null, null, null);
    }

    /** Begins the next one-card pick for each remaining card type, with random bottoming at the end. */
    public static LibrarySearchFollowUp forCardTypeBoundedPick(List<CardType> types) {
        return forCardTypeBoundedPick(types, LibrarySearchDestination.HAND);
    }

    /** Begins the next one-card pick for each remaining card type. */
    public static LibrarySearchFollowUp forCardTypeBoundedPick(List<CardType> types,
                                                               LibrarySearchDestination destination) {
        if (types.isEmpty()) {
            return forBoundedPick(SecondBoundedPick.terminal(true, destination));
        }
        return forBoundedPick(SecondBoundedPick.cardType(
                types.getFirst(), types.subList(1, types.size()), true, destination));
    }

    /** Begins a bounded subtype-pick flow, optionally randomizing the cards left on the bottom. */
    public static LibrarySearchFollowUp forSubtypeBoundedPick(List<CardSubtype> subtypes,
                                                               boolean randomRest) {
        return forSubtypeBoundedPick(subtypes, randomRest, LibrarySearchDestination.HAND);
    }

    /** Begins a bounded subtype-pick flow with the chosen-card destination preserved. */
    public static LibrarySearchFollowUp forSubtypeBoundedPick(List<CardSubtype> subtypes,
                                                               boolean randomRest,
                                                               LibrarySearchDestination destination) {
        if (subtypes.isEmpty()) {
            return forBoundedPick(SecondBoundedPick.terminal(randomRest, destination));
        }
        return forBoundedPick(SecondBoundedPick.subtype(
                subtypes.getFirst(), subtypes.subList(1, subtypes.size()), randomRest, destination));
    }

    public static LibrarySearchFollowUp forBoundedPick(SecondBoundedPick pick) {
        return new LibrarySearchFollowUp(null, null, List.of(), false, null, null, List.of(), 0, false, List.of(),
                pick, null, List.of(), null, null, null);
    }

    public static LibrarySearchFollowUp eachPlayerBasicLand(List<UUID> remainingSearchers, boolean tapped) {
        return new LibrarySearchFollowUp(null, null, remainingSearchers, tapped, null, null, List.of(), 0, false, List.of(),
                null, null, List.of(), null, null, null);
    }

    /**
     * The APNAP remainder of an "each player may search their library for up to {@code count} cards
     * to hand" flow. {@code creatureOnly} restricts the search to creature cards and reveals the
     * chosen cards (Weird Harvest); an unrestricted search takes any card without revealing it
     * (Noble Benefactor).
     */
    public static LibrarySearchFollowUp eachPlayerCardsToHand(List<UUID> remainingSearchers, int count,
                                                              boolean creatureOnly) {
        return new LibrarySearchFollowUp(null, null, List.of(), false, null, null, remainingSearchers, count,
                creatureOnly, List.of(), null, null, List.of(), null, null, null);
    }

    public static LibrarySearchFollowUp eachPlayerCreatureToBattlefield(List<UUID> remainingSearchers) {
        return new LibrarySearchFollowUp(null, null, List.of(), false, null, null, List.of(), 0, false, remainingSearchers,
                null, null, List.of(), null, null, null);
    }

    public static LibrarySearchFollowUp eachPlayerLandToBattlefield(List<UUID> remainingSearchers) {
        return new LibrarySearchFollowUp(null, null, List.of(), false, null, null, List.of(), 0, false, List.of(),
                List.of(), null, null, List.of(), null, null, null, remainingSearchers);
    }

    /** The remaining targeted players in a multi-player search-to-top effect. */
    public static LibrarySearchFollowUp targetPlayersLibraryToTop(List<UUID> remainingSearchers) {
        return new LibrarySearchFollowUp(null, null, List.of(), false, null, null, List.of(), 0, false, List.of(),
                remainingSearchers, null, null, List.of(), null, null, null);
    }

    public static LibrarySearchFollowUp opponentExile(PendingOpponentExileChoice choice) {
        return new LibrarySearchFollowUp(null, null, List.of(), false, choice, null, List.of(), 0, false, List.of(), null,
                null, List.of(), null, null, null);
    }

    public static LibrarySearchFollowUp imprint(UUID sourcePermanentId) {
        return new LibrarySearchFollowUp(null, null, List.of(), false, null, sourcePermanentId, List.of(), 0, false,
                List.of(), null, null, List.of(), null, null, null);
    }

    /** The queue of permanent names still to search for, one entry per permanent (Clarion Ultimatum, Doubling Chant). */
    public static LibrarySearchFollowUp sameNamePicks(List<String> names, boolean creatureOnly,
                                                      LibrarySearchDestination destination) {
        return new LibrarySearchFollowUp(null, null, List.of(), false, null, null, List.of(), 0, false, List.of(), null,
                new SameNamePickQueue(names, creatureOnly, destination), List.of(), null, null, null);
    }

    /** The same-name queue with a separately searched library and battlefield controller. */
    public static LibrarySearchFollowUp sameNamePicks(List<String> names, boolean creatureOnly,
                                                      LibrarySearchDestination destination,
                                                      UUID libraryOwnerId, UUID battlefieldControllerId) {
        return new LibrarySearchFollowUp(null, null, List.of(), false, null, null, List.of(), 0, false, List.of(), null,
                new SameNamePickQueue(names, creatureOnly, destination, libraryOwnerId, battlefieldControllerId, true),
                List.of(), null, null, null);
    }

    /** The same-name queue with explicit mandatory-versus-optional searches. */
    public static LibrarySearchFollowUp sameNamePicks(List<String> names, boolean creatureOnly,
                                                      LibrarySearchDestination destination,
                                                      UUID libraryOwnerId, UUID battlefieldControllerId,
                                                      boolean optional) {
        return new LibrarySearchFollowUp(null, null, List.of(), false, null, null, List.of(), 0, false, List.of(), null,
                new SameNamePickQueue(names, creatureOnly, destination, libraryOwnerId, battlefieldControllerId, optional),
                List.of(), null, null, null);
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
        return new LibrarySearchFollowUp(null, null, List.of(), false, null, null, List.of(), 0, false, List.of(), null, null,
                picks, null, null, null);
    }

    /**
     * The queue of exact mana values still to search for, one instant card per value to hand
     * (Firemind's Foresight). {@code null} on the follow-up means this mechanic is absent;
     * an empty list means every value has already been searched (shuffle once).
     */
    public static LibrarySearchFollowUp instantManaValueToHandPicks(List<Integer> manaValues) {
        return new LibrarySearchFollowUp(null, null, List.of(), false, null, null, List.of(), 0, false, List.of(), null, null,
                List.of(), List.copyOf(manaValues), null, null);
    }

    /** The remaining APNAP per-player basic-land picks plus any forced sacrifices that follow them. */
    public static LibrarySearchFollowUp basicLandSearches(List<BasicLandsPick> remainingPicks,
                                                        List<PendingForcedSacrifice> sacrifices) {
        return new LibrarySearchFollowUp(null, null, List.of(), false, null, null, List.of(), 0, false, List.of(), null,
                null, List.of(), null, new BasicLandSearchQueue(remainingPicks, sacrifices), null);
    }

    /** Completion data for a reveal-only search followed by Grim Reminder's life-loss clause. */
    public static LibrarySearchFollowUp grimReminderSearch(int lifeLoss) {
        return new LibrarySearchFollowUp(null, null, List.of(), false, null, null, List.of(), 0, false, List.of(),
                null, null, List.of(), null, null, new GrimReminderSearch(lifeLoss));
    }

    /** The same follow-up with the each-player basic-land remainder advanced past the current searcher. */
    public LibrarySearchFollowUp withRemainingEachPlayerBasicLandSearches(List<UUID> remaining) {
        return new LibrarySearchFollowUp(basicLandToHand, cardToGraveyard, remaining,
                eachPlayerSearchTapped, opponentExileChoice, imprintSourcePermanentId,
                remainingEachPlayerToHandSearches, eachPlayerToHandCount, eachPlayerToHandCreatureOnly,
                remainingEachPlayerCreatureToBattlefieldSearches, secondBoundedPick, remainingSameNamePicks,
                remainingToHandPicks, remainingInstantManaValueToHandPicks, basicLandSearchQueue, grimReminderSearch);
    }

    /** The same follow-up with the each-player creature-to-hand remainder advanced past the current searcher. */
    public LibrarySearchFollowUp withRemainingEachPlayerToHandSearches(List<UUID> remaining) {
        return new LibrarySearchFollowUp(basicLandToHand, cardToGraveyard,
                remainingEachPlayerBasicLandSearches, eachPlayerSearchTapped, opponentExileChoice,
                imprintSourcePermanentId, remaining, eachPlayerToHandCount, eachPlayerToHandCreatureOnly,
                remainingEachPlayerCreatureToBattlefieldSearches, secondBoundedPick, remainingSameNamePicks,
                remainingToHandPicks, remainingInstantManaValueToHandPicks, basicLandSearchQueue, grimReminderSearch);
    }

    /** The same follow-up with the each-player creature-to-battlefield remainder advanced past the current searcher. */
    public LibrarySearchFollowUp withRemainingEachPlayerCreatureToBattlefieldSearches(List<UUID> remaining) {
        return new LibrarySearchFollowUp(basicLandToHand, cardToGraveyard,
                remainingEachPlayerBasicLandSearches, eachPlayerSearchTapped, opponentExileChoice,
                imprintSourcePermanentId, remainingEachPlayerToHandSearches,
                eachPlayerToHandCount, eachPlayerToHandCreatureOnly, remaining, secondBoundedPick, remainingSameNamePicks,
                remainingToHandPicks, remainingInstantManaValueToHandPicks, basicLandSearchQueue, grimReminderSearch);
    }

    /** The same follow-up with the opponent land-search remainder advanced past the current searcher. */
    public LibrarySearchFollowUp withRemainingEachPlayerLandToBattlefieldSearches(List<UUID> remaining) {
        return new LibrarySearchFollowUp(basicLandToHand, cardToGraveyard,
                remainingEachPlayerBasicLandSearches, eachPlayerSearchTapped, opponentExileChoice,
                imprintSourcePermanentId, remainingEachPlayerToHandSearches,
                eachPlayerToHandCount, eachPlayerToHandCreatureOnly,
                remainingEachPlayerCreatureToBattlefieldSearches, remainingTargetPlayerTopSearches,
                secondBoundedPick, remainingSameNamePicks, remainingToHandPicks,
                remainingInstantManaValueToHandPicks, basicLandSearchQueue, grimReminderSearch, remaining);
    }

    /** The same follow-up with the targeted top-search remainder advanced past the current player. */
    public LibrarySearchFollowUp withRemainingTargetPlayerTopSearches(List<UUID> remaining) {
        return new LibrarySearchFollowUp(basicLandToHand, cardToGraveyard,
                remainingEachPlayerBasicLandSearches, eachPlayerSearchTapped, opponentExileChoice,
                imprintSourcePermanentId, remainingEachPlayerToHandSearches,
                eachPlayerToHandCount, eachPlayerToHandCreatureOnly,
                remainingEachPlayerCreatureToBattlefieldSearches, remaining,
                secondBoundedPick, remainingSameNamePicks, remainingToHandPicks,
                remainingInstantManaValueToHandPicks, basicLandSearchQueue, grimReminderSearch);
    }

    /** The same follow-up with the same-name-pick queue advanced past the current name. */
    public LibrarySearchFollowUp withRemainingSameNamePicks(SameNamePickQueue remaining) {
        return new LibrarySearchFollowUp(basicLandToHand, cardToGraveyard,
                remainingEachPlayerBasicLandSearches, eachPlayerSearchTapped, opponentExileChoice,
                imprintSourcePermanentId, remainingEachPlayerToHandSearches,
                eachPlayerToHandCount, eachPlayerToHandCreatureOnly, remainingEachPlayerCreatureToBattlefieldSearches,
                secondBoundedPick, remaining, remainingToHandPicks, remainingInstantManaValueToHandPicks,
                basicLandSearchQueue, grimReminderSearch);
    }

    /** The same follow-up with the to-hand pick queue advanced past the current pick. */
    public LibrarySearchFollowUp withRemainingToHandPicks(List<ToHandPick> remaining) {
        return new LibrarySearchFollowUp(basicLandToHand, cardToGraveyard,
                remainingEachPlayerBasicLandSearches, eachPlayerSearchTapped, opponentExileChoice,
                imprintSourcePermanentId, remainingEachPlayerToHandSearches,
                eachPlayerToHandCount, eachPlayerToHandCreatureOnly, remainingEachPlayerCreatureToBattlefieldSearches,
                secondBoundedPick, remainingSameNamePicks, remaining,
                remainingInstantManaValueToHandPicks, basicLandSearchQueue, grimReminderSearch);
    }

    /**
     * The same follow-up with the instant-mana-value-to-hand queue advanced past the current value
     * (Firemind's Foresight).
     */
    public LibrarySearchFollowUp withRemainingInstantManaValueToHandPicks(List<Integer> remaining) {
        return new LibrarySearchFollowUp(basicLandToHand, cardToGraveyard,
                remainingEachPlayerBasicLandSearches, eachPlayerSearchTapped, opponentExileChoice,
                imprintSourcePermanentId, remainingEachPlayerToHandSearches,
                eachPlayerToHandCount, eachPlayerToHandCreatureOnly, remainingEachPlayerCreatureToBattlefieldSearches,
                secondBoundedPick, remainingSameNamePicks, remainingToHandPicks, remaining, basicLandSearchQueue,
                grimReminderSearch);
    }

    /** The same follow-up with Natural Balance's pick queue advanced past the current searcher. */
    public LibrarySearchFollowUp withBasicLandSearchQueue(BasicLandSearchQueue queue) {
        return new LibrarySearchFollowUp(basicLandToHand, cardToGraveyard,
                remainingEachPlayerBasicLandSearches, eachPlayerSearchTapped, opponentExileChoice,
                imprintSourcePermanentId, remainingEachPlayerToHandSearches,
                eachPlayerToHandCount, eachPlayerToHandCreatureOnly, remainingEachPlayerCreatureToBattlefieldSearches,
                secondBoundedPick, remainingSameNamePicks, remainingToHandPicks,
                remainingInstantManaValueToHandPicks, queue, grimReminderSearch);
    }

    /** The same follow-up with the consumed basic-land-to-hand pick cleared. */
    public LibrarySearchFollowUp clearBasicLandToHand() {
        return withBasicLandToHand(null);
    }

    /** The same follow-up with the basic-land-to-hand pick replaced (null = no picks left). */
    public LibrarySearchFollowUp withBasicLandToHand(BasicLandToHandPick pick) {
        return new LibrarySearchFollowUp(pick, cardToGraveyard, remainingEachPlayerBasicLandSearches,
                eachPlayerSearchTapped, opponentExileChoice, imprintSourcePermanentId,
                remainingEachPlayerToHandSearches, eachPlayerToHandCount, eachPlayerToHandCreatureOnly,
                remainingEachPlayerCreatureToBattlefieldSearches, secondBoundedPick, remainingSameNamePicks,
                remainingToHandPicks, remainingInstantManaValueToHandPicks, basicLandSearchQueue, grimReminderSearch);
    }

    /** The same follow-up with the consumed card-to-graveyard pick cleared. */
    public LibrarySearchFollowUp clearCardToGraveyard() {
        return new LibrarySearchFollowUp(basicLandToHand, null, remainingEachPlayerBasicLandSearches,
                eachPlayerSearchTapped, opponentExileChoice, imprintSourcePermanentId,
                remainingEachPlayerToHandSearches, eachPlayerToHandCount, eachPlayerToHandCreatureOnly,
                remainingEachPlayerCreatureToBattlefieldSearches, secondBoundedPick, remainingSameNamePicks,
                remainingToHandPicks, remainingInstantManaValueToHandPicks, basicLandSearchQueue, grimReminderSearch);
    }
}
