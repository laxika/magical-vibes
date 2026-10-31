package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp.SameNamePickQueue;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.PendingEachPlayerLibraryExile;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CantSearchLibrariesEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LibrarySearchCastPermission;
import com.github.laxika.magicalvibes.model.effect.LibraryNinjutsuEffect;
import com.github.laxika.magicalvibes.model.effect.OppositionAgentEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantSearchLibrariesAtAllEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantSearchLibrariesEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentSearchesTopCardsInsteadEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardHasAllCardNamesPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.library.LibrarySearchTriggerHelper;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Shared library search helpers used by every normal Library Search effect handler and by
 * input handlers that continue async search flows.
 *
 * <p>Extracted verbatim from the original {@code LibrarySearchResolutionService} monolith;
 * behavior (log strings, interaction ordering, shuffle timing) is identical.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LibrarySearchSupport {

    private final GameLogService gameLogService;
    private final com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry interactionHandlerRegistry;
    private final GameQueryService gameQueryService;
    @Autowired
    private ReverseMiracleSupport reverseMiracleSupport;
    @Autowired
    @Lazy
    private TriggerCollectionService triggerCollectionService;

    /**
     * Starts the next pending "each player searches for a basic land" search from the
     * follow-up's remaining-searchers list; the advanced remainder rides the begun search.
     * Returns true if a search was initiated, false if no searcher remains.
     * Respects {@code followUp.eachPlayerSearchTapped()} for the destination.
     */
    public boolean startNextEachPlayerBasicLandSearch(GameData gameData, LibrarySearchFollowUp followUp) {
        return startNextEachPlayerBasicLandSearch(gameData, followUp, true);
    }

    /**
     * Starts the next each-player basic-land search, optionally using a mandatory-search prompt.
     * A restricted search can still fail to find a matching card even when the instruction is not
     * optional.
     */
    public boolean startNextEachPlayerBasicLandSearch(GameData gameData, LibrarySearchFollowUp followUp,
                                                       boolean maySearch) {
        LibrarySearchDestination destination = followUp.eachPlayerSearchTapped()
                ? LibrarySearchDestination.BATTLEFIELD_TAPPED
                : LibrarySearchDestination.BATTLEFIELD;
        String prompt = maySearch
                ? followUp.eachPlayerSearchTapped()
                        ? "You may search your library for a basic land card and put it onto the battlefield tapped."
                        : "You may search your library for a basic land card and put it onto the battlefield."
                : followUp.eachPlayerSearchTapped()
                        ? "Search your library for a basic land card and put it onto the battlefield tapped."
                        : "Search your library for a basic land card and put it onto the battlefield.";

        List<UUID> remaining = new ArrayList<>(followUp.remainingEachPlayerBasicLandSearches());
        while (!remaining.isEmpty()) {
            UUID nextPlayerId = remaining.remove(0);
            LibrarySearchFollowUp nextFollowUp = followUp.withRemainingEachPlayerBasicLandSearches(remaining);
            if (maySearch) {
                List<Card> deck = gameData.playerDecks.get(nextPlayerId);
                if (deck != null && !deck.isEmpty() && !isSearchPrevented(gameData, nextPlayerId)
                        && opponentSearchTopCardsLimit(gameData, nextPlayerId) == Integer.MAX_VALUE) {
                    // The optional search is offered even when the library holds no basic land: the
                    // player may decline, and declining means they never searched and do not shuffle.
                    sendLibrarySearchToPlayer(gameData, nextPlayerId,
                            LibrarySearchParams.builder(nextPlayerId,
                                            deck.stream()
                                                    .filter(card -> card.hasType(CardType.LAND)
                                                            && gameQueryService.cardHasSupertype(card,
                                                            CardSupertype.BASIC, gameData, nextPlayerId))
                                                    .toList())
                                    .canFailToFind(true)
                                    .prompt(prompt)
                                    .destination(destination)
                                    .followUp(nextFollowUp)
                                    .declineSkipsShuffle(true)
                                    .build(),
                            prompt, true, gameData.playerIdToName.get(nextPlayerId) + " may search their library.");
                    return true;
                }
            }
            boolean started = performLibrarySearch(
                    gameData,
                    nextPlayerId,
                    card -> card.hasType(CardType.LAND)
                            && gameQueryService.cardHasSupertype(card, CardSupertype.BASIC, gameData, nextPlayerId),
                    "basic land cards",
                    prompt,
                    false,
                    true,
                    destination,
                    nextFollowUp
            );
            if (started) {
                return true;
            }
            // If search could not start (empty library, Leonin Arbiter, etc.), try the next player
        }
        return false;
    }

    /**
     * Starts the next pending "each player may search for up to N cards to hand" search from the
     * follow-up's remaining-searchers list; the advanced remainder rides the begun search. Each
     * searcher may take up to {@code followUp.eachPlayerToHandCount()} cards to hand, then shuffles;
     * when {@code followUp.eachPlayerToHandCreatureOnly()} the search is restricted to creature cards
     * and the taken cards are revealed. Returns true if a search was initiated, false if no searcher
     * remains (Leonin Arbiter players are skipped; a non-creature search with an empty library or no
     * matching cards is skipped, while a creature-only search is always offered). Used by
     * Weird Harvest (creature-only) and Noble Benefactor (any card).
     */
    public boolean startNextEachPlayerToHandSearch(GameData gameData, LibrarySearchFollowUp followUp) {
        int count = followUp.eachPlayerToHandCount();
        boolean creatureOnly = followUp.eachPlayerToHandCreatureOnly();
        List<UUID> remaining = new ArrayList<>(followUp.remainingEachPlayerToHandSearches());
        while (!remaining.isEmpty()) {
            UUID nextPlayerId = remaining.remove(0);
            String playerName = gameData.playerIdToName.get(nextPlayerId);

            if (isSearchPrevented(gameData, nextPlayerId, false)) {
                continue;
            }

            List<Card> deck = gameData.playerDecks.get(nextPlayerId);
            List<Card> library = deck == null ? List.of() : deck;
            if (library.isEmpty() && !creatureOnly) {
                // "May search": a player with nothing to find isn't made to search without choosing to.
                continue;
            }

            List<Card> choices = creatureOnly
                    ? library.stream().filter(card -> card.hasAllCardNames() || card.hasType(CardType.CREATURE)).toList()
                    : library;
            choices = restrictToTopCards(gameData, nextPlayerId, choices,
                    opponentSearchTopCardsLimit(gameData, nextPlayerId));

            // A creature-only "may search" is always offered, even with nothing to find, because searching
            // is the player's choice (it triggers opponents' search abilities and shuffles the library).
            // With a count of zero the player may search for no cards, so nothing can be picked.
            if (creatureOnly && count == 0) {
                choices = List.of();
            }
            if (choices.isEmpty() && !creatureOnly) {
                LibrarySearchTriggerHelper.recordSearchAndQueueTriggers(gameData, gameLogService, nextPlayerId);
                LibraryShuffleHelper.shuffleLibrary(gameData, nextPlayerId);
                gameLogService.append(gameData, GameLog.text(playerName + " searches their library but finds no creature cards. Library is shuffled."));
                continue;
            }

            String prompt = creatureOnly
                    ? "You may search your library for up to " + count + " creature card"
                            + (count == 1 ? "" : "s") + " to reveal and put into your hand."
                    : "You may search your library for up to " + count + " card"
                            + (count == 1 ? "" : "s") + " to put into your hand.";
            LibrarySearchParams params = LibrarySearchParams.builder(nextPlayerId, new ArrayList<>(choices))
                    .reveals(creatureOnly)
                    .canFailToFind(true)
                    .remainingCount(count)
                    .destination(LibrarySearchDestination.HAND)
                    .filterPredicate(creatureOnly
                            ? new CardAnyOfPredicate(List.of(
                            new CardTypePredicate(CardType.CREATURE), new CardHasAllCardNamesPredicate()))
                            : null)
                    .followUp(followUp.withRemainingEachPlayerToHandSearches(remaining))
                    .build();

            sendLibrarySearchToPlayer(gameData, nextPlayerId, params, prompt, true);
            return true;
        }
        revealDeferredEachPlayerToHandPicks(gameData);
        return false;
    }

    /**
     * Once every player has made their choices, the cards they picked are revealed and put into their
     * hands at the same time (CR 101.4), in APNAP order for the log.
     */
    private void revealDeferredEachPlayerToHandPicks(GameData gameData) {
        if (gameData.eachPlayerSearchDeferredToHand.isEmpty()) {
            return;
        }
        List<UUID> order = new ArrayList<>();
        order.add(gameData.activePlayerId);
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(gameData.activePlayerId)) {
                order.add(playerId);
            }
        }
        Map<UUID, List<Card>> picks = new LinkedHashMap<>();
        for (UUID playerId : order) {
            List<Card> cards = gameData.eachPlayerSearchDeferredToHand.remove(playerId);
            if (cards != null && !cards.isEmpty()) {
                picks.put(playerId, cards);
            }
        }
        gameData.eachPlayerSearchDeferredToHand.clear();
        picks.forEach((playerId, cards) -> {
            GameLog.Builder reveal = GameLog.builder().text(gameData.playerIdToName.get(playerId) + " reveals ");
            for (int i = 0; i < cards.size(); i++) {
                if (i > 0) {
                    reveal.text(", ");
                }
                reveal.card(cards.get(i));
            }
            gameLogService.append(gameData, reveal.text(" and puts "
                    + (cards.size() == 1 ? "it" : "them") + " into their hand.").build());
        });
        picks.forEach((playerId, cards) -> cards.forEach(card -> {
            gameData.addCardToHand(playerId, card);
            triggerCollectionService.checkControllerCardPutIntoHandFromLibraryTriggers(gameData, playerId, card);
        }));
    }

    /**
     * Starts the next pending "each opponent may search for a creature card to battlefield" search
     * from the follow-up's remaining-searchers list; the advanced remainder rides the begun search.
     * Each searcher may search their library for a creature card, put it onto the battlefield, then
     * shuffle. Returns true if a search was initiated, false if no searcher remains (empty library /
     * no creatures / Leonin Arbiter players are skipped). Used by Boldwyr Heavyweights.
     */
    public boolean startNextEachPlayerCreatureToBattlefieldSearch(GameData gameData, LibrarySearchFollowUp followUp) {
        List<UUID> remaining = new ArrayList<>(followUp.remainingEachPlayerCreatureToBattlefieldSearches());
        while (!remaining.isEmpty()) {
            UUID nextPlayerId = remaining.remove(0);
            boolean started = performLibrarySearch(
                    gameData,
                    nextPlayerId,
                    card -> card.hasType(CardType.CREATURE),
                    "creature cards",
                    "You may search your library for a creature card and put it onto the battlefield.",
                    false,
                    true,
                    LibrarySearchDestination.BATTLEFIELD,
                    followUp.withRemainingEachPlayerCreatureToBattlefieldSearches(remaining)
            );
            if (started) {
                return true;
            }
            // If search could not start (empty library, no creatures, Leonin Arbiter, etc.), try the next player
        }
        return false;
    }

    /**
     * Starts the next pending "each opponent may search for a land card to battlefield" search
     * from the follow-up's remaining-searchers list. Each searcher may take one land card, which
     * enters untapped, then shuffles.
     */
    public boolean startNextEachPlayerLandToBattlefieldSearch(GameData gameData,
                                                               LibrarySearchFollowUp followUp) {
        List<UUID> remaining = new ArrayList<>(followUp.remainingEachPlayerLandToBattlefieldSearches());
        while (!remaining.isEmpty()) {
            UUID nextPlayerId = remaining.remove(0);
            LibrarySearchFollowUp nextFollowUp = followUp.withRemainingEachPlayerLandToBattlefieldSearches(remaining);
            String prompt = "You may search your library for a land card and put it onto the battlefield.";
            List<Card> deck = gameData.playerDecks.get(nextPlayerId);
            boolean started;
            if (deck != null && !deck.isEmpty() && !isSearchPrevented(gameData, nextPlayerId)
                    && opponentSearchTopCardsLimit(gameData, nextPlayerId) == Integer.MAX_VALUE) {
                // The search is optional even when the library holds no land card: the player may
                // decline, in which case they never searched and do not shuffle.
                sendLibrarySearchToPlayer(gameData, nextPlayerId,
                        LibrarySearchParams.builder(nextPlayerId,
                                        deck.stream().filter(card -> card.hasType(CardType.LAND)).toList())
                                .canFailToFind(true)
                                .prompt(prompt)
                                .destination(LibrarySearchDestination.BATTLEFIELD)
                                .followUp(nextFollowUp)
                                .declineSkipsShuffle(true)
                                .build(),
                        prompt, true);
                started = true;
            } else {
                started = performLibrarySearch(
                        gameData,
                        nextPlayerId,
                        card -> card.hasType(CardType.LAND),
                        "land cards",
                        prompt,
                        false,
                        true,
                        LibrarySearchDestination.BATTLEFIELD,
                        nextFollowUp
                );
            }
            if (started) {
                return true;
            }
        }
        return false;
    }

    /**
     * Starts the next targeted player's mandatory unrestricted search for a card to put on top of
     * their library. Players whose searches cannot start are skipped, while the remaining players
     * continue through the shared library-search interaction flow.
     */
    public boolean startNextTargetPlayerTopSearch(GameData gameData, LibrarySearchFollowUp followUp) {
        List<UUID> remaining = new ArrayList<>(followUp.remainingTargetPlayerTopSearches());
        while (!remaining.isEmpty()) {
            UUID playerId = remaining.remove(0);
            boolean started = performLibrarySearch(
                    gameData,
                    playerId,
                    card -> true,
                    "cards",
                    "Search your library for a card, then shuffle and put that card on top.",
                    false,
                    false,
                    LibrarySearchDestination.TOP_OF_LIBRARY,
                    followUp.withRemainingTargetPlayerTopSearches(remaining));
            if (started) {
                return true;
            }
        }
        return false;
    }

    /**
     * Starts the next pending "search for a card with the same name and put it onto the battlefield"
     * pick from the follow-up's same-name queue (Clarion Ultimatum, Doubling Chant). Each queue entry
     * is one permanent's name; the queue's own destination and creature-only restriction apply to
     * every pick, and the advanced remainder rides the begun search. Names with no matching card in
     * the library are skipped. A queue may also identify a different library owner and battlefield
     * controller, as used by Dichotomancy. A simultaneous queue is a single search: the first call
     * records it and fixes the library cards it can find, and later picks stay within those cards.
     * Returns true if a search was initiated, false if the queue is exhausted or absent, search is
     * prevented, or the library is empty.
     */
    public boolean startNextSameNamePick(GameData gameData, UUID playerId, LibrarySearchFollowUp followUp) {
        SameNamePickQueue startingQueue = followUp.remainingSameNamePicks();
        if (startingQueue == null) return false;

        UUID libraryOwnerId = startingQueue.libraryOwnerId() != null ? startingQueue.libraryOwnerId() : playerId;
        if (isSearchPrevented(gameData, playerId, libraryOwnerId, true, playerId)) return false;

        if (startingQueue.simultaneous() && startingQueue.searchableCardIds() == null) {
            List<Card> library = gameData.playerDecks.getOrDefault(libraryOwnerId, List.of());
            int limit = Math.min(opponentSearchTopCardsLimit(gameData, playerId), library.size());
            startingQueue = startingQueue.withSearchableCardIds(library.subList(0, limit).stream()
                    .map(Card::getId).collect(java.util.stream.Collectors.toSet()));
            followUp = followUp.withRemainingSameNamePicks(startingQueue);
            if (libraryOwnerId.equals(playerId)) {
                LibrarySearchTriggerHelper.recordSearchAndQueueTriggers(gameData, gameLogService, playerId);
            }
        }
        SameNamePickQueue queue = startingQueue;

        boolean tapped = queue.destination() == LibrarySearchDestination.BATTLEFIELD_TAPPED;
        List<Card> deck = gameData.playerDecks.get(libraryOwnerId);
        List<String> remaining = new ArrayList<>(queue.names());
        while (!remaining.isEmpty()) {
            String name = remaining.remove(0);
            if (deck == null || deck.isEmpty()) {
                return false;
            }
            List<Card> matches = deck.stream()
                    .filter(card -> !queue.selectedCards().contains(card))
                    .filter(card -> card.hasAllCardNames() || name.equals(card.getName()))
                    .filter(card -> !queue.creatureOnly() || card.hasType(CardType.CREATURE))
                    .filter(card -> queue.searchableCardIds() == null
                            || queue.searchableCardIds().contains(card.getId()))
                    .toList();
            int topLimit = opponentSearchTopCardsLimit(gameData, playerId);
            if (queue.searchableCardIds() == null && topLimit != Integer.MAX_VALUE) {
                matches = restrictToTopCards(gameData, libraryOwnerId, matches, topLimit);
            }
            if (matches.isEmpty()) {
                continue;
            }
            String libraryDescription = queue.libraryOwnerId() == null
                    ? "your library" : gameData.playerIdToName.get(libraryOwnerId) + "'s library";
            String prompt = (queue.optional() ? "You may search " : "Search ") + libraryDescription + " for a "
                    + (queue.creatureOnly() ? "creature card" : "card")
                    + " named " + name + " and put it onto the battlefield" + (tapped ? " tapped." : ".");
            sendLibrarySearchToPlayer(gameData, playerId,
                    LibrarySearchParams.builder(playerId, new ArrayList<>(matches))
                            .targetPlayerId(queue.libraryOwnerId())
                            .canFailToFind(true)
                            .filterCardName(name)
                            .destination(queue.destination())
                            .battlefieldControllerId(queue.battlefieldControllerId())
                            .shuffleAfterSelection(!queue.simultaneous())
                            .followUp(followUp.withRemainingSameNamePicks(queue.withNames(remaining)))
                            .build(), prompt, true,
                    gameData.playerIdToName.get(playerId) + " searches " + libraryDescription.replace("your", "their") + ".",
                    queue.searchableCardIds());
            return true;
        }
        return false;
    }

    /**
     * Advances the queued {@link PendingEachPlayerLibraryExile}: begins the controller's search of
     * the next player's library for a nonland card to exile, skipping players whose library holds
     * no nonland card (they still shuffle) and players nobody may search (Leonin Arbiter). Once
     * every library has been searched the accumulated exiled cards are offered for free casting.
     *
     * <p>Returns {@code true} when a search or the free-cast choice was begun, {@code false} when
     * nothing remains to do and the caller should finish the resolution itself.
     */
    public boolean advanceEachPlayerNonlandExile(GameData gameData) {
        PendingEachPlayerLibraryExile state = gameData.pollPendingInteraction(PendingEachPlayerLibraryExile.class);
        if (state == null) {
            return false;
        }

        UUID searcherId = state.searcherId();
        String searcherName = gameData.playerIdToName.get(searcherId);
        List<UUID> remaining = new ArrayList<>(state.remainingPlayerIds());

        while (!remaining.isEmpty()) {
            UUID targetPlayerId = remaining.removeFirst();
            String targetName = gameData.playerIdToName.get(targetPlayerId);

            if (isSearchPrevented(gameData, searcherId, targetPlayerId, true, searcherId)) {
                LibraryShuffleHelper.shuffleLibrary(gameData, targetPlayerId);
                gameLogService.append(gameData, GameLog.text(targetName + "'s library is shuffled."));
                continue;
            }

            List<Card> deck = gameData.playerDecks.get(targetPlayerId);
            List<Card> nonland = deck == null ? List.of()
                    : deck.stream().filter(card -> card.hasAllCardNames() || !card.hasType(CardType.LAND)).toList();
            if (nonland.isEmpty()) {
                LibraryShuffleHelper.shuffleLibrary(gameData, targetPlayerId);
                gameLogService.append(gameData, GameLog.text(searcherName + " finds no nonland card in "
                        + targetName + "'s library. Library is shuffled."));
                continue;
            }

            gameData.queueInteraction(new PendingEachPlayerLibraryExile(searcherId, remaining, state.exiledCardIds()));
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearch(
                    LibrarySearchParams.builder(searcherId, new ArrayList<>(nonland))
                            .targetPlayerId(targetPlayerId)
                            .canFailToFind(true)
                            .destination(LibrarySearchDestination.EXILE_FOR_FREE_CAST)
                            .shuffleAfterSelection(true)
                            .build(),
                    "Search " + targetName + "'s library for a nonland card to exile.", false));
            gameLogService.append(gameData, GameLog.text(searcherName + " searches " + targetName + "'s library."));
            return true;
        }

        List<UUID> castable = state.exiledCardIds().stream()
                .filter(cardId -> {
                    var exiled = gameData.findExiledCard(cardId);
                    return exiled != null && !exiled.card().hasType(CardType.LAND);
                })
                .toList();
        if (castable.isEmpty()) {
            return false;
        }
        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.ImprovisationCapstoneCastChoice(searcherId, castable, castable.size()));
        return true;
    }

    /**
     * Starts the next "search for a card matching the queued descriptor, reveal it, put it into your
     * hand" pick from the follow-up's queue (a colour for Conflux, a subtype for Gem of Becoming, or
     * an exact card name for Nissa's Encouragement). Each queue entry is one descriptor, searched in
     * order; the advanced remainder rides the begun search. Descriptors with no matching card left
     * in the library are skipped without shuffling. When the queue is exhausted the library is
     * shuffled once (the single shuffle for the whole search) and false is returned; returns true if
     * a search was begun, false if the search is prevented or no descriptor remains to search.
     */
    /** Starts several choices within one search, retaining its original searchable library cards. */
    public boolean startSingleSearchToHandPicks(GameData gameData, UUID playerId,
                                               LibrarySearchFollowUp followUp) {
        if (isSearchPrevented(gameData, playerId)) return false;
        List<Card> deck = gameData.playerDecks.getOrDefault(playerId, List.of());
        int limit = Math.min(opponentSearchTopCardsLimit(gameData, playerId), deck.size());
        Set<UUID> searchableCardIds = deck.subList(0, limit).stream()
                .map(Card::getId).collect(java.util.stream.Collectors.toSet());
        LibrarySearchTriggerHelper.recordSearchAndQueueTriggers(gameData, gameLogService, playerId);
        return startNextToHandPick(gameData, playerId, followUp.withRemainingToHandPicks(
                followUp.remainingToHandPicks().stream()
                        .map(pick -> pick.withinSingleSearch(searchableCardIds)).toList()));
    }

    public boolean startNextToHandPick(GameData gameData, UUID playerId, LibrarySearchFollowUp followUp) {
        if (isSearchPrevented(gameData, playerId)) return false;

        List<Card> deck = gameData.playerDecks.get(playerId);
        String playerName = gameData.playerIdToName.get(playerId);
        List<LibrarySearchFollowUp.ToHandPick> remaining = new ArrayList<>(followUp.remainingToHandPicks());
        while (!remaining.isEmpty()) {
            LibrarySearchFollowUp.ToHandPick pick = remaining.remove(0);
            List<Card> matches = new ArrayList<>(deck == null ? List.of()
                    : deck.stream()
                            .filter(card -> pick.searchableCardIds() == null
                                    || pick.searchableCardIds().contains(card.getId()))
                            .filter(card -> matchesToHandPick(card, pick)).toList());
            if (pick.includeGraveyard()) {
                gameData.playerGraveyards.getOrDefault(playerId, List.of()).stream()
                        .filter(card -> matchesToHandPick(card, pick))
                        .forEach(matches::add);
            }
            if (matches.isEmpty()) {
                continue;
            }
            String prompt;
            String logMsg;
            var builder = LibrarySearchParams.builder(playerId, new ArrayList<>(matches))
                    .reveals(true)
                    .canFailToFind(true)
                    .destination(LibrarySearchDestination.HAND)
                    .shuffleAfterSelection(remaining.isEmpty())
                    .followUp(followUp.withRemainingToHandPicks(remaining));
            if (pick.cardName() != null) {
                builder.filterCardName(pick.cardName());
                prompt = "Search your " + (pick.includeGraveyard() ? "library and graveyard" : "library")
                        + " for a card named " + pick.cardName() + " to reveal and put into your hand.";
                logMsg = playerName + " searches their library for a card named " + pick.cardName() + ".";
            } else {
                String descriptor = pick.describe();
                prompt = "Search your library for a " + descriptor + " card to reveal and put into your hand.";
                logMsg = playerName + " searches their library for a " + descriptor + " card.";
            }
            sendLibrarySearchToPlayer(gameData, playerId, builder.build(), prompt, true, logMsg,
                    pick.searchableCardIds());
            return true;
        }

        // Every descriptor has been searched; the single shuffle for the whole search happens now.
        if (deck != null) {
            LibraryShuffleHelper.shuffleLibrary(gameData, playerId);
        }
        gameLogService.append(gameData, GameLog.text(playerName + "'s library is shuffled."));
        return false;
    }

    /**
     * Begins one search of {@code playerId}'s library for a card of each of {@code subtypes} (and of
     * {@code requiredType} when non-null) that are put onto the battlefield together (Gaea's Balance).
     * The search is recorded once, and the cards it can find are fixed when it begins. Returns true
     * if a pick was begun. Otherwise nothing can be found: the library is shuffled and false is
     * returned.
     */
    public boolean startSubtypeToBattlefieldPicks(GameData gameData, UUID playerId,
                                                  List<com.github.laxika.magicalvibes.model.CardSubtype> subtypes,
                                                  CardType requiredType, LibrarySearchDestination destination) {
        if (isSearchPrevented(gameData, playerId)) return false;
        List<Card> deck = gameData.playerDecks.getOrDefault(playerId, List.of());
        int limit = Math.min(opponentSearchTopCardsLimit(gameData, playerId), deck.size());
        Set<UUID> searchableCardIds = deck.subList(0, limit).stream()
                .map(Card::getId).collect(java.util.stream.Collectors.toSet());
        LibrarySearchTriggerHelper.recordSearchAndQueueTriggers(gameData, gameLogService, playerId);
        if (startNextSubtypeToBattlefieldPick(gameData, playerId, LibrarySearchFollowUp.subtypeToBattlefieldPicks(
                subtypes, requiredType, destination, searchableCardIds))) {
            return true;
        }
        LibraryShuffleHelper.shuffleLibrary(gameData, playerId);
        gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(playerId)
                + " searches their library and finds no matching cards. Library is shuffled."));
        return false;
    }

    /**
     * Begins one search of {@code playerId}'s library for a card of each of {@code colors}, all put
     * into {@code destination} together after the last pick (Lotuslight Dancers). The search is
     * recorded once, and the cards it can find are fixed when it begins. A multicoloured card may fill
     * any one colour but only one. Returns true if a pick was begun. Otherwise nothing can be found:
     * the library is shuffled and false is returned.
     */
    public boolean startColorPicks(GameData gameData, UUID playerId, List<CardColor> colors,
                                   LibrarySearchDestination destination) {
        if (isSearchPrevented(gameData, playerId)) return false;
        List<Card> deck = gameData.playerDecks.getOrDefault(playerId, List.of());
        int limit = Math.min(opponentSearchTopCardsLimit(gameData, playerId), deck.size());
        Set<UUID> searchableCardIds = deck.subList(0, limit).stream()
                .map(Card::getId).collect(java.util.stream.Collectors.toSet());
        LibrarySearchTriggerHelper.recordSearchAndQueueTriggers(gameData, gameLogService, playerId);
        if (startNextSubtypeToBattlefieldPick(gameData, playerId,
                LibrarySearchFollowUp.colorPicks(colors, destination, searchableCardIds))) {
            return true;
        }
        LibraryShuffleHelper.shuffleLibrary(gameData, playerId);
        gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(playerId)
                + " searches their library and finds no matching cards. Library is shuffled."));
        return false;
    }

    /**
     * Offers the next pick of the follow-up's {@link LibrarySearchFollowUp.SubtypeToBattlefieldPickQueue},
     * skipping subtypes with no unselected matching card among the search's cards. Returns false
     * once no pick remains; the caller then puts the selected cards onto the battlefield and
     * shuffles.
     */
    public boolean startNextSubtypeToBattlefieldPick(GameData gameData, UUID playerId,
                                                     LibrarySearchFollowUp followUp) {
        LibrarySearchFollowUp.SubtypeToBattlefieldPickQueue queue = followUp.subtypeToBattlefieldPicks();
        if (queue == null) return false;
        List<Card> deck = gameData.playerDecks.getOrDefault(playerId, List.of());
        Set<UUID> selectedIds = queue.selectedCards().stream()
                .map(Card::getId).collect(java.util.stream.Collectors.toSet());
        List<com.github.laxika.magicalvibes.model.CardSubtype> remaining = new ArrayList<>(queue.remainingSubtypes());
        while (!remaining.isEmpty()) {
            com.github.laxika.magicalvibes.model.CardSubtype subtype = remaining.removeFirst();
            List<Card> matches = deck.stream()
                    .filter(card -> queue.searchableCardIds().contains(card.getId()))
                    .filter(card -> !selectedIds.contains(card.getId()))
                    .filter(card -> card.getSubtypes().contains(subtype))
                    .filter(card -> queue.requiredType() == null || card.hasType(queue.requiredType()))
                    .toList();
            if (matches.isEmpty()) {
                continue;
            }
            String descriptor = subtype.getDisplayName()
                    + (queue.requiredType() == null ? "" : " " + queue.requiredType().getDisplayName().toLowerCase());
            String playerName = gameData.playerIdToName.get(playerId);
            sendLibrarySearchToPlayer(gameData, playerId,
                    LibrarySearchParams.builder(playerId, new ArrayList<>(matches))
                            .canFailToFind(true)
                            .destination(queue.destination())
                            .shuffleAfterSelection(false)
                            .followUp(followUp.withSubtypeToBattlefieldPicks(queue.withRemainingSubtypes(remaining)))
                            .build(),
                    "Search your library for a " + descriptor + " card to put onto the battlefield.",
                    true,
                    playerName + " searches their library for a " + descriptor + " card.",
                    queue.searchableCardIds());
            return true;
        }
        List<CardColor> remainingColors = new ArrayList<>(queue.remainingColors());
        while (!remainingColors.isEmpty()) {
            CardColor color = remainingColors.removeFirst();
            List<Card> matches = deck.stream()
                    .filter(card -> queue.searchableCardIds().contains(card.getId()))
                    .filter(card -> !selectedIds.contains(card.getId()))
                    .filter(card -> card.getColors().contains(color))
                    .filter(card -> queue.requiredType() == null || card.hasType(queue.requiredType()))
                    .toList();
            if (matches.isEmpty()) {
                continue;
            }
            String descriptor = color.name().toLowerCase();
            String playerName = gameData.playerIdToName.get(playerId);
            String destinationText = queue.destination() == LibrarySearchDestination.GRAVEYARD
                    ? "into your graveyard" : "onto the battlefield";
            sendLibrarySearchToPlayer(gameData, playerId,
                    LibrarySearchParams.builder(playerId, new ArrayList<>(matches))
                            .canFailToFind(true)
                            .destination(queue.destination())
                            .shuffleAfterSelection(false)
                            .followUp(followUp.withSubtypeToBattlefieldPicks(
                                    queue.withRemainingSubtypes(List.of()).withRemainingColors(remainingColors)))
                            .build(),
                    "Search your library for a " + descriptor + " card to put " + destinationText + ".",
                    true,
                    playerName + " searches their library for a " + descriptor + " card.",
                    queue.searchableCardIds());
            return true;
        }
        return false;
    }

    private static boolean matchesToHandPick(Card card, LibrarySearchFollowUp.ToHandPick pick) {
        if (card.hasAllCardNames()) {
            return true;
        }
        if (pick.cardName() != null) {
            return pick.cardName().equals(card.getName());
        }
        if (pick.basicOnly() && !(card.getSupertypes().contains(CardSupertype.BASIC) && card.hasType(CardType.LAND))) {
            return false;
        }
        return pick.color() != null
                ? card.getColors().contains(pick.color())
                : card.getSubtypes().contains(pick.subtype());
    }

    /**
     * Starts the next "search for an instant card with the queued mana value, reveal it, put it into
     * your hand" pick from the follow-up's mana-value queue (Firemind's Foresight). Each queue entry
     * is one exact mana value, searched in order; values with no matching instant left in the library
     * are skipped without shuffling. When the queue is exhausted the library is shuffled once and
     * false is returned; returns true if a search was begun.
     */
    public boolean startNextInstantManaValueToHandPick(GameData gameData, UUID playerId,
                                                       LibrarySearchFollowUp followUp) {
        if (isSearchPrevented(gameData, playerId)) return false;

        List<Integer> queue = followUp.remainingInstantManaValueToHandPicks();
        if (queue == null) return false;

        List<Card> deck = gameData.playerDecks.get(playerId);
        String playerName = gameData.playerIdToName.get(playerId);
        List<Integer> remaining = new ArrayList<>(queue);
        while (!remaining.isEmpty()) {
            int manaValue = remaining.remove(0);
            List<Card> matches = deck == null ? List.of()
                    : deck.stream()
                            .filter(card -> card.hasAllCardNames()
                                    || (card.hasType(CardType.INSTANT) && card.getManaValue() == manaValue))
                            .toList();
            if (matches.isEmpty()) {
                continue;
            }
            sendLibrarySearchToPlayer(gameData, playerId,
                    LibrarySearchParams.builder(playerId, new ArrayList<>(matches))
                            .reveals(true)
                            .canFailToFind(true)
                            .destination(LibrarySearchDestination.HAND)
                            .shuffleAfterSelection(false)
                            .followUp(followUp.withRemainingInstantManaValueToHandPicks(remaining))
                            .build(),
                    "Search your library for an instant card with mana value " + manaValue
                            + " to reveal and put into your hand.", true,
                    playerName + " searches their library for an instant card with mana value "
                            + manaValue + ".");
            return true;
        }

        if (deck != null) {
            LibraryShuffleHelper.shuffleLibrary(gameData, playerId);
        }
        gameLogService.append(gameData, GameLog.text(playerName + "'s library is shuffled."));
        return false;
    }

    /**
     * Searches the controller's library for a creature card, reveals it, and puts it into their hand.
     * Called after the sacrifice portion of SacrificeCreatureSearchLibraryForCreatureToHandEffect completes.
     */
    public void searchLibraryForCreatureToHand(GameData gameData, UUID controllerId) {
        performLibrarySearch(
                gameData,
                controllerId,
                card -> card.hasType(CardType.CREATURE),
                "creature cards",
                "Search your library for a creature card to reveal and put into your hand.",
                true,
                true,
                LibrarySearchDestination.HAND
        );
    }

    /**
     * Unified library search skeleton: check restriction -> get deck -> filter -> handle no matches ->
     * begin interaction -> send message -> log. Returns true if the search was initiated, false otherwise.
     */
    public boolean performLibrarySearch(
            GameData gameData,
            UUID controllerId,
            Predicate<Card> filter,
            String noMatchDescription,
            String prompt,
            boolean reveals,
            boolean canFailToFind,
            LibrarySearchDestination destination) {
        return performLibrarySearch(gameData, controllerId, filter, noMatchDescription, prompt,
                reveals, canFailToFind, destination, LibrarySearchFollowUp.NONE);
    }

    public boolean performLibrarySearch(
            GameData gameData,
            UUID controllerId,
            Predicate<Card> filter,
            String noMatchDescription,
            String prompt,
            boolean reveals,
            boolean canFailToFind,
            LibrarySearchDestination destination,
            LibrarySearchFollowUp followUp) {
        return performLibrarySearch(gameData, controllerId, filter, noMatchDescription, prompt,
                reveals, canFailToFind, destination, followUp, null);
    }

    /**
     * @param attachToPermanentId when non-null (paired with
     *        {@link LibrarySearchDestination#BATTLEFIELD_ATTACHED_TO_PERMANENT}), the found card
     *        enters the battlefield attached to that permanent.
     */
    public boolean performLibrarySearch(
            GameData gameData,
            UUID controllerId,
            Predicate<Card> filter,
            String noMatchDescription,
            String prompt,
            boolean reveals,
            boolean canFailToFind,
            LibrarySearchDestination destination,
            LibrarySearchFollowUp followUp,
            UUID attachToPermanentId) {
        return performLibrarySearch(gameData, controllerId, filter, noMatchDescription, prompt,
                reveals, canFailToFind, destination, followUp, attachToPermanentId, null);
    }

    public boolean performLibrarySearch(
            GameData gameData,
            UUID controllerId,
            Predicate<Card> filter,
            String noMatchDescription,
            String prompt,
            boolean reveals,
            boolean canFailToFind,
            LibrarySearchDestination destination,
            LibrarySearchFollowUp followUp,
            UUID attachToPermanentId,
            Integer mayCastManaValueAtMost) {
        return performLibrarySearch(gameData, controllerId, filter, noMatchDescription, prompt,
                reveals, canFailToFind, destination, followUp, attachToPermanentId,
                mayCastManaValueAtMost, true);
    }

    public boolean performLibrarySearch(
            GameData gameData,
            UUID controllerId,
            Predicate<Card> filter,
            String noMatchDescription,
            String prompt,
            boolean reveals,
            boolean canFailToFind,
            LibrarySearchDestination destination,
            LibrarySearchFollowUp followUp,
            UUID attachToPermanentId,
            Integer mayCastManaValueAtMost,
            boolean shuffleAfterSelection) {
        if (isSearchPrevented(gameData, controllerId)) return false;

        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);

        if (deck == null || deck.isEmpty()) {
            // Searching an empty library is still a search, so opponent-search triggers fire here too
            // (the interaction-starting path fires them in sendLibrarySearchToPlayer).
            LibrarySearchTriggerHelper.recordSearchAndQueueTriggers(gameData, gameLogService, controllerId);
            if (shuffleAfterSelection) {
                LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            }
            String logMsg = playerName + " searches their library but it is empty."
                    + (shuffleAfterSelection ? " Library is shuffled." : "");
            gameLogService.append(gameData, GameLog.text(logMsg));
            return false;
        }

        List<Card> matchingCards = deck.stream()
                .filter(card -> card.hasAllCardNames() || filter == null || filter.test(card))
                .toList();
        int topLimit = opponentSearchTopCardsLimit(gameData, controllerId);
        if (topLimit != Integer.MAX_VALUE) {
            matchingCards = restrictToTopCards(gameData, controllerId, matchingCards, topLimit);
        }

        if (matchingCards.isEmpty()) {
            LibrarySearchTriggerHelper.recordSearchAndQueueTriggers(gameData, gameLogService, controllerId);
            if (shuffleAfterSelection) {
                LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            }
            String logMsg = playerName + " searches their library but finds no " + noMatchDescription + "."
                    + (shuffleAfterSelection ? " Library is shuffled." : "");
            gameLogService.append(gameData, GameLog.text(logMsg));
            log.info("Game {} - {} searches library, no {} found", gameData.id, playerName, noMatchDescription);
            return false;
        }

        sendLibrarySearchToPlayer(gameData, controllerId, LibrarySearchParams.builder(controllerId, matchingCards)
                .reveals(reveals)
                .canFailToFind(canFailToFind)
                .prompt(prompt)
                .destination(destination)
                .shuffleAfterSelection(shuffleAfterSelection)
                .followUp(followUp)
                .attachToPermanentId(attachToPermanentId)
                .mayCastManaValueAtMost(mayCastManaValueAtMost)
                .build(), prompt, canFailToFind);

        log.info("Game {} - {} searches their library ({} matches)", gameData.id, playerName, matchingCards.size());
        return true;
    }

    /**
     * Checks whether a library search is prevented by a static restriction. Payment for a
     * pay-to-ignore restriction is made by {@code searchingPlayerId}; the library owner is
     * separate because some effects make one player search another player's library.
     */
    public boolean checkSearchRestriction(GameData gameData, UUID searchingPlayerId) {
        UUID causingControllerId = resolvingControllerId(gameData);
        return checkSearchRestriction(gameData, searchingPlayerId, searchingPlayerId, causingControllerId);
    }

    public boolean checkSearchRestriction(GameData gameData, UUID searchingPlayerId,
                                          UUID causingControllerId) {
        return checkSearchRestriction(gameData, searchingPlayerId, searchingPlayerId, causingControllerId);
    }

    public boolean checkSearchRestriction(GameData gameData, UUID searchingPlayerId,
                                          UUID libraryOwnerId, UUID causingControllerId) {
        if (gameData.playersCantSearchLibrariesThisTurn) {
            String playerName = gameData.playerIdToName.get(searchingPlayerId);
            gameLogService.append(gameData, GameLog.text(
                    playerName + "'s library search is prevented this turn."));
            log.info("Game {} - {} search prevented this turn", gameData.id, playerName);
            return false;
        }

        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                for (CardEffect effect : gameQueryService.getActiveStaticEffects(gameData, perm)) {
                    if (effect instanceof OpponentsCantSearchLibrariesAtAllEffect) {
                        if (pid.equals(searchingPlayerId)) {
                            continue;
                        }
                        String playerName = gameData.playerIdToName.get(searchingPlayerId);
                        String sourceName = perm.getCard().getName();
                        gameLogService.append(gameData, GameLog.text(
                                playerName + "'s library search is prevented by " + sourceName + "."));
                        log.info("Game {} - {} search prevented by {}",
                                gameData.id, playerName, sourceName);
                        return false;
                    }
                    if (effect instanceof OpponentsCantSearchLibrariesEffect) {
                        if (causingControllerId == null
                                || !libraryOwnerId.equals(causingControllerId)
                                || pid.equals(causingControllerId)) {
                            continue;
                        }
                        String playerName = gameData.playerIdToName.get(searchingPlayerId);
                        String sourceName = perm.getCard().getName();
                        gameLogService.append(gameData, GameLog.text(
                                playerName + "'s library search is prevented by " + sourceName + "."));
                        log.info("Game {} - {} search prevented by {}",
                                gameData.id, playerName, sourceName);
                        return false;
                    }
                    if (effect instanceof CantSearchLibrariesEffect restriction) {
                        boolean paid = false;
                        if (restriction.payableToIgnore()) {
                            Set<UUID> paidSet = gameData.paidSearchTaxPermanentIds.get(searchingPlayerId);
                            paid = paidSet != null && paidSet.contains(perm.getId());
                        }
                        if (!paid) {
                            String playerName = gameData.playerIdToName.get(searchingPlayerId);
                            String sourceName = perm.getCard().getName();
                            String logMsg = playerName + "'s library search is prevented by " + sourceName + ".";
                            gameLogService.append(gameData, GameLog.text(logMsg));
                            log.info("Game {} - {} search prevented by {}",
                                    gameData.id, playerName, sourceName);
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    private UUID resolvingControllerId(GameData gameData) {
        if (gameData.currentlyResolvingControllerId != null) {
            return gameData.currentlyResolvingControllerId;
        }
        StackEntry pendingEntry = gameData.pendingEffectResolutionEntry;
        return pendingEntry == null ? null : pendingEntry.getControllerId();
    }

    public String formatCardTypeSetForPrompt(Set<CardType> cardTypes) {
        if (cardTypes == null || cardTypes.isEmpty()) {
            return "matching";
        }
        List<String> names = cardTypes.stream()
                .map(type -> type.name().toLowerCase())
                .sorted()
                .toList();
        if (names.size() == 1) {
            return names.getFirst();
        }
        return String.join(" or ", names);
    }

    public static boolean matchesCardTypes(Card card, Set<CardType> cardTypes) {
        return cardTypes.contains(card.getType())
                || card.getAdditionalTypes().stream().anyMatch(cardTypes::contains);
    }

    /** Returns whether {@code card} carries a permission to be cast during its owner's library search. */
    public boolean isLibrarySearchCastableCard(Card card) {
        return card.getStaticEffects().stream()
                .anyMatch(LibrarySearchCastPermission.class::isInstance);
    }

    /** Returns the library-ninjutsu permission carried by {@code card}, if any. */
    public LibraryNinjutsuEffect libraryNinjutsuEffect(Card card) {
        return card.getStaticEffects().stream()
                .filter(LibraryNinjutsuEffect.class::isInstance)
                .map(LibraryNinjutsuEffect.class::cast)
                .findFirst()
                .orElse(null);
    }

    /** Returns the cards in {@code playerId}'s library that may be cast during that search. */
    public List<Card> librarySearchCastableCards(GameData gameData, UUID playerId) {
        List<Card> deck = gameData.playerDecks.get(playerId);
        if (deck == null) return List.of();
        return deck.stream().filter(this::isLibrarySearchCastableCard).toList();
    }

    /** Applies the controller of the most recently entered opposing Opposition Agent, if any. */
    public LibrarySearchParams applyOppositionAgentControl(GameData gameData, LibrarySearchParams params) {
        if (params.decisionPlayerId() != null
                || params.sourceSideboard()
                || params.sourceCards() != null
                || (params.targetPlayerId() != null && !params.targetPlayerId().equals(params.playerId()))) {
            return params;
        }

        UUID controllerId = oppositionAgentControllerId(gameData, params.playerId());
        return controllerId == null ? params : params.withDecisionPlayerId(controllerId);
    }

    /**
     * Returns the controller of the most recently entered Opposition Agent controlled by an
     * opponent of {@code searcherId}, or {@code null} when no opposing Agent is on the battlefield.
     */
    public UUID oppositionAgentControllerId(GameData gameData, UUID searcherId) {
        UUID controllerId = null;
        long newestTimestamp = Long.MIN_VALUE;
        for (UUID battlefieldControllerId : gameData.orderedPlayerIds) {
            if (battlefieldControllerId.equals(searcherId)) {
                continue;
            }
            List<Permanent> battlefield = gameData.playerBattlefields.get(battlefieldControllerId);
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                boolean isOppositionAgent = permanent.getCard().getStaticEffects().stream()
                        .anyMatch(OppositionAgentEffect.class::isInstance)
                        && !gameQueryService.hasLostAllAbilities(gameData, permanent);
                if (isOppositionAgent && permanent.getTimestamp() >= newestTimestamp) {
                    newestTimestamp = permanent.getTimestamp();
                    controllerId = battlefieldControllerId;
                }
            }
        }
        return controllerId;
    }

    public void sendLibrarySearchToPlayer(GameData gameData, UUID playerId, LibrarySearchParams params,
                                            String prompt, boolean canFailToFind) {
        String playerName = gameData.playerIdToName.get(playerId);
        sendLibrarySearchToPlayer(gameData, playerId, params, prompt, canFailToFind,
                playerName + " searches their library.");
    }

    public void sendLibrarySearchToPlayer(GameData gameData, UUID playerId, LibrarySearchParams params,
                                            String prompt, boolean canFailToFind, String logMessage) {
        sendLibrarySearchToPlayer(gameData, playerId, params, prompt, canFailToFind, logMessage, null);
    }

    private void sendLibrarySearchToPlayer(GameData gameData, UUID playerId, LibrarySearchParams params,
                                           String prompt, boolean canFailToFind, String logMessage,
                                           Set<UUID> singleSearchCardIds) {
        // Universal choke point for every library search that presents cards: fire
        // ON_OPPONENT_SEARCHES_LIBRARY (Ob Nixilis, Unshackled) for a player searching their OWN
        // library. An optional search that can be declined (declineSkipsShuffle) only begins with the
        // player's first pick, so its triggers are fired there instead. A search of someone else's library (targetPlayerId set) is not "their library".
        if (singleSearchCardIds == null
                && !gameData.suppressNextLibrarySearchTriggers
                && (params.targetPlayerId() == null || params.targetPlayerId().equals(params.playerId()))
                && params.followUp().basicLandSearchQueue() == null
                && params.followUp().eachPlayerToHandCount() == 0
                && !params.declineSkipsShuffle()) {
            LibrarySearchTriggerHelper.recordSearchAndQueueTriggers(gameData, gameLogService, params.playerId());
        }

        // Aven Mindcensor & friends: an opponent's search is limited to the top N cards of that library.
        int topLimit = singleSearchCardIds != null ? Integer.MAX_VALUE
                : opponentSearchTopCardsLimit(gameData, params.playerId());
        if (params.topLibraryCardLimit() > 0) {
            topLimit = Math.min(topLimit, params.topLibraryCardLimit());
        }
        if (topLimit != Integer.MAX_VALUE) {
            UUID libraryOwnerId = params.targetPlayerId() != null ? params.targetPlayerId() : params.playerId();
            List<Card> restricted = restrictToTopCards(gameData, libraryOwnerId, params.cards(), topLimit);
            if (restricted.isEmpty() && !params.cards().isEmpty()) {
                // None of the top N cards match the search: the player searched but found nothing.
                gameLogService.append(gameData, GameLog.text(logMessage));
                if (params.shuffleAfterSelection()) {
                    LibraryShuffleHelper.shuffleLibrary(gameData, libraryOwnerId);
                }
                String searcherName = gameData.playerIdToName.get(params.playerId());
                gameLogService.append(gameData, GameLog.text(
                        searcherName + " finds no matching card among the top " + topLimit
                                + " cards."
                                + (params.shuffleAfterSelection() ? " Library is shuffled." : "")));
                return;
            }
            params = params.withCards(restricted);
        }

        boolean ownLibrarySearch = (params.targetPlayerId() == null || params.targetPlayerId().equals(playerId))
                && !params.sourceSideboard()
                && params.sourceCards() == null;
        if (ownLibrarySearch) {
            params = params.withAllowCastFromLibraryWhileSearching(true);
            List<Card> castableCards = librarySearchCastableCards(gameData, playerId);
            if (singleSearchCardIds != null) {
                castableCards = castableCards.stream()
                        .filter(card -> singleSearchCardIds.contains(card.getId())).toList();
            }
            if (topLimit != Integer.MAX_VALUE) {
                castableCards = restrictToTopCards(gameData, playerId, castableCards, topLimit);
            }
            if (!castableCards.isEmpty()) {
                Set<UUID> existingCardIds = params.cards().stream().map(Card::getId).collect(java.util.stream.Collectors.toSet());
                List<Card> cards = new ArrayList<>(params.cards());
                castableCards.stream()
                        .filter(card -> !existingCardIds.contains(card.getId()))
                        .forEach(cards::add);
                if (cards.size() > params.cards().size()) {
                    Set<UUID> castOnlyCardIds = castableCards.stream().map(Card::getId)
                            .filter(id -> !existingCardIds.contains(id))
                            .collect(java.util.stream.Collectors.toSet());
                    params = params.withCards(cards).withCastOnlyCardIds(castOnlyCardIds);
                    prompt += " You may also cast a card with a library-search permission.";
                }
            }
        }

        params = applyOppositionAgentControl(gameData, params);

        if (reverseMiracleSupport != null && reverseMiracleSupport.offerBeforeSearch(gameData, playerId, params,
                prompt, canFailToFind, logMessage)) {
            return;
        }

        interactionHandlerRegistry.begin(gameData, new com.github.laxika.magicalvibes.model.PendingInteraction.LibrarySearch(
                params, prompt, canFailToFind));

        gameLogService.append(gameData, GameLog.text(logMessage));
    }

    /**
     * The strictest "search only the top N cards" limit imposed on {@code searchingPlayerId} by an
     * {@link OpponentSearchesTopCardsInsteadEffect} (Aven Mindcensor) that one of their opponents
     * controls, or {@link Integer#MAX_VALUE} when none applies. A player's own copy never limits
     * their own searches — the effect only cares about opponents.
     */
    public int opponentSearchTopCardsLimit(GameData gameData, UUID searchingPlayerId) {
        int limit = Integer.MAX_VALUE;
        for (UUID pid : gameData.orderedPlayerIds) {
            if (pid.equals(searchingPlayerId)) continue;
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                for (CardEffect effect : gameQueryService.getActiveStaticEffects(gameData, perm)) {
                    if (effect instanceof OpponentSearchesTopCardsInsteadEffect restriction) {
                        limit = Math.min(limit, restriction.count());
                    }
                }
            }
        }
        return limit;
    }

    /**
     * Narrows {@code candidates} to only the cards that are among the top {@code limit} cards of
     * {@code libraryOwnerId}'s library, preserving candidate order. Uses reference identity so a
     * duplicate-named card deeper in the library is not wrongly treated as searchable.
     */
    private List<Card> restrictToTopCards(GameData gameData, UUID libraryOwnerId, List<Card> candidates, int limit) {
        List<Card> deck = gameData.playerDecks.get(libraryOwnerId);
        if (deck == null || deck.isEmpty()) return candidates;
        List<Card> top = deck.subList(0, Math.min(limit, deck.size()));
        Set<Card> topCards = Collections.newSetFromMap(new IdentityHashMap<>());
        topCards.addAll(top);
        return candidates.stream().filter(topCards::contains).toList();
    }

    public boolean isSearchPrevented(GameData gameData, UUID searchingPlayerId) {
        UUID causingControllerId = resolvingControllerId(gameData);
        return isSearchPrevented(gameData, searchingPlayerId, searchingPlayerId, true, causingControllerId);
    }

    public boolean isSearchPrevented(GameData gameData, UUID searchingPlayerId, boolean shuffleWhenPrevented) {
        UUID causingControllerId = resolvingControllerId(gameData);
        return isSearchPrevented(gameData, searchingPlayerId, searchingPlayerId, shuffleWhenPrevented,
                causingControllerId);
    }

    public boolean isSearchPrevented(GameData gameData, UUID searchingPlayerId,
                                     UUID causingControllerId) {
        return isSearchPrevented(gameData, searchingPlayerId, searchingPlayerId, true, causingControllerId);
    }

    public boolean isSearchPrevented(GameData gameData, UUID searchingPlayerId, UUID libraryOwnerId,
                                     boolean shuffleWhenPrevented, UUID causingControllerId) {
        if (checkSearchRestriction(gameData, searchingPlayerId, libraryOwnerId, causingControllerId)) return false;
        List<Card> deck = gameData.playerDecks.get(libraryOwnerId);
        if (shuffleWhenPrevented && deck != null) {
            LibraryShuffleHelper.shuffleLibrary(gameData, libraryOwnerId);
        }
        return true;
    }

    public void putHandOnTopOfLibrary(GameData gameData, List<Card> hand, List<Card> deck, String playerName) {
        int handSize = hand.size();
        if (handSize == 0) return;
        for (int i = handSize - 1; i >= 0; i--) {
            deck.addFirst(hand.get(i));
        }
        hand.clear();
        String logMsg = playerName + " puts " + pluralCards(handSize)
                + " from their hand on top of their library.";
        gameLogService.append(gameData, GameLog.text(logMsg));
    }

    public static String pluralCards(int count) {
        return count + " card" + (count != 1 ? "s" : "");
    }
}
