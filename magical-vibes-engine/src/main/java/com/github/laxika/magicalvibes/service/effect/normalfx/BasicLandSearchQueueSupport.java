package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.BattlefieldEntryCard;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardHasAllCardNamesPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryBatchSupport;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.library.LibrarySearchTriggerHelper;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Drives an each-player library search: first the APNAP queue of per-player "you may search your library for up to X
 * [basic land] cards and put them onto the battlefield or into your hand" picks (the queue's filter says which cards),
 * then any forced land sacrifices held on the same {@link LibrarySearchFollowUp.BasicLandSearchQueue}. The queue is
 * advanced from the effect handler and re-entered by the library-search input handler after every pick resolves. Used
 * by {@code EachPlayerSearchesLibraryEffect} and by Natural Balance (searches then sacrifices).
 */
@Component
@RequiredArgsConstructor
public class BasicLandSearchQueueSupport {

    private final LibrarySearchSupport librarySearchSupport;
    private final DestructionSupport destructionSupport;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final BattlefieldEntryBatchSupport battlefieldEntryBatchSupport;
    private final PredicateEvaluationService predicateEvaluationService;

    /** Active player first, then every other player in seating order (CR 101.4 APNAP). */
    public List<UUID> apnapOrder(GameData gameData) {
        UUID activePlayerId = gameData.activePlayerId;
        List<UUID> ordered = new ArrayList<>();
        if (gameData.orderedPlayerIds.contains(activePlayerId)) {
            ordered.add(activePlayerId);
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(activePlayerId)) {
                ordered.add(playerId);
            }
        }
        return ordered;
    }

    /**
     * Begins the next outstanding piece of work on {@code queue}: the next player's basic-land
     * search if one can start, otherwise the forced sacrifices. Returns true when an interaction
     * was begun (the caller must not finish the resolution), false when the whole queue is done.
     */
    public boolean advance(GameData gameData, LibrarySearchFollowUp followUp) {
        LibrarySearchFollowUp.BasicLandSearchQueue queue = followUp.basicLandSearchQueue();
        if (queue == null) {
            return false;
        }

        List<LibrarySearchFollowUp.BasicLandsPick> remaining = new ArrayList<>(queue.remainingPicks());
        while (!remaining.isEmpty()) {
            LibrarySearchFollowUp.BasicLandsPick pick = remaining.removeFirst();
            LibrarySearchFollowUp.BasicLandSearchQueue nextQueue = queue.withRemainingPicks(remaining);
            // "May search" that can't be done isn't chosen (CR 608.2d), so only a search that was instructed still
            // shuffles the library of a player who can't search, unless a later step does the shuffling.
            boolean searchAllowed = !librarySearchSupport.isSearchPrevented(
                    gameData, pick.playerId(), !markSearchedOnPick(queue) && !queue.leavesLibrariesUnshuffled());
            if (searchAllowed && !markSearchedOnPick(queue)) {
                nextQueue = nextQueue.withSearchedPlayer(pick.playerId());
            }
            if (searchAllowed && startSearch(gameData, pick, followUp.withBasicLandSearchQueue(nextQueue))) {
                return true;
            }
            queue = nextQueue;
        }

        placeSelectedLands(gameData, queue);
        shuffleAfterQueue(gameData, queue);
        return beginSacrifices(gameData, queue);
    }

    /**
     * Whether a player counts as having searched only once they take a card: an optional search whose
     * searchers shuffle together after the queue ("each player who searched this way shuffles") leaves
     * the player who declines unshuffled.
     */
    public static boolean markSearchedOnPick(LibrarySearchFollowUp.BasicLandSearchQueue queue) {
        return queue.optionalSearch() && queue.shuffleAfterQueue();
    }

    /** Moves what the whole queue found at once (CR 608.2e): into the searchers' hands, or onto the battlefield. */
    private void placeSelectedLands(GameData gameData, LibrarySearchFollowUp.BasicLandSearchQueue queue) {
        if (queue.selectedLands().isEmpty()) return;
        if (queue.destinationToHand()) {
            putSelectedCardsIntoHands(gameData, queue);
            return;
        }
        List<BattlefieldEntryCard> entering = new ArrayList<>();
        for (LibrarySearchFollowUp.DeferredBasicLand selected : queue.selectedLands()) {
            UUID controllerId = battlefieldEntryService.resolveEnteringController(
                    gameData, selected.battlefieldControllerId(), new Permanent(selected.card()));
            entering.add(new BattlefieldEntryCard(controllerId, selected.libraryOwnerId(), selected.card(),
                    Zone.LIBRARY, null).withTapped(selected.enterTapped()));
        }
        battlefieldEntryBatchSupport.begin(gameData, entering);
    }

    /** A search for cards with a stated quality reveals what it found; a search for any card shows nothing. */
    private void putSelectedCardsIntoHands(GameData gameData, LibrarySearchFollowUp.BasicLandSearchQueue queue) {
        Map<UUID, List<Card>> found = new LinkedHashMap<>();
        for (LibrarySearchFollowUp.DeferredBasicLand selected : queue.selectedLands()) {
            List<Card> library = gameData.playerDecks.get(selected.libraryOwnerId());
            if (library != null && library.remove(selected.card())) {
                found.computeIfAbsent(selected.libraryOwnerId(), ignored -> new ArrayList<>()).add(selected.card());
            }
        }
        found.forEach((playerId, cards) -> {
            String playerName = gameData.playerIdToName.get(playerId);
            if (reveals(queue)) {
                GameLog.Builder reveal = GameLog.builder().text(playerName + " reveals ");
                for (int i = 0; i < cards.size(); i++) {
                    if (i > 0) {
                        reveal.text(", ");
                    }
                    reveal.card(cards.get(i));
                }
                gameLogService.append(gameData, reveal.text(" and puts "
                        + (cards.size() == 1 ? "it" : "them") + " into their hand.").build());
            } else {
                gameLogService.append(gameData, GameLog.text(playerName + " puts " + cards.size() + " card"
                        + (cards.size() == 1 ? "" : "s") + " into their hand."));
            }
            cards.forEach(card -> gameData.addCardToHand(playerId, card));
        });
    }

    private static boolean reveals(LibrarySearchFollowUp.BasicLandSearchQueue queue) {
        return queue.destinationToHand() && !(queue.filter() instanceof CardTruePredicate);
    }

    private boolean startSearch(GameData gameData, LibrarySearchFollowUp.BasicLandsPick pick,
            LibrarySearchFollowUp followUp) {
        UUID playerId = pick.playerId();
        LibrarySearchFollowUp.BasicLandSearchQueue queue = followUp.basicLandSearchQueue();
        boolean shuffleAfterQueue = queue.shuffleAfterQueue();
        boolean choosesWhetherToSearch = markSearchedOnPick(queue);
        String playerName = gameData.playerIdToName.get(playerId);
        String described = CardPredicateUtils.describeFilter(queue.filter());
        List<Card> deck = gameData.playerDecks.get(playerId);
        if (deck == null || deck.isEmpty()) {
            if (choosesWhetherToSearch) {
                // Nobody is made to search without choosing to: the choice is offered where searches are watched, and
                // for a search that reveals what it finds (Weird Harvest).
                if (deck == null || !reveals(queue)
                        && !LibrarySearchTriggerHelper.anyPermanentReactsToLibrarySearches(gameData)) {
                    return false;
                }
            } else {
                LibrarySearchTriggerHelper.recordSearchAndQueueTriggers(gameData, playerId);
                if (!shuffleAfterQueue && deck != null) LibraryShuffleHelper.shuffleLibrary(gameData, playerId);
                gameLogService.append(gameData,
                        GameLog.text(playerName + " searches their library but it is empty."
                                + (shuffleAfterQueue ? "" : " Library is shuffled.")));
                return false;
            }
        }

        // Aven Mindcensor: only the top cards are searched, so only they can be found. A player who may search is
        // asked whatever those cards and the ones below them hold: whether a prompt comes, and what is logged, tells
        // nothing of a library not searched yet (CR 701.23a).
        int searchable = Math.min(deck.size(), librarySearchSupport.opponentSearchTopCardsLimit(gameData, playerId));
        // A card an earlier search of the queue found waits in the library for the others; it can't be found again.
        java.util.Set<UUID> alreadyFound = queue.selectedLands().stream()
                .map(selected -> selected.card().getId())
                .collect(java.util.stream.Collectors.toSet());
        List<Card> matching = deck.subList(0, searchable).stream()
                .filter(card -> !alreadyFound.contains(card.getId()))
                .filter(card -> card.hasAllCardNames() || predicateEvaluationService.matchesCardPredicate(
                        card, queue.filter(), null, gameData, playerId))
                .toList();
        if (matching.isEmpty() && !choosesWhetherToSearch) {
            LibrarySearchTriggerHelper.recordSearchAndQueueTriggers(gameData, playerId);
            if (!shuffleAfterQueue) LibraryShuffleHelper.shuffleLibrary(gameData, playerId);
            gameLogService.append(gameData, GameLog.text(
                    playerName + " searches their library but finds no " + described + "s."
                            + (shuffleAfterQueue ? "" : " Library is shuffled.")));
            return false;
        }

        int count = pick.count();
        if (count <= 0 && !choosesWhetherToSearch) {
            LibrarySearchTriggerHelper.recordSearchAndQueueTriggers(gameData, playerId);
            if (!shuffleAfterQueue) LibraryShuffleHelper.shuffleLibrary(gameData, playerId);
            gameLogService.append(gameData, GameLog.text(
                    playerName + " searches their library for up to zero " + described + "s."
                            + (shuffleAfterQueue ? "" : " Library is shuffled.")));
            return false;
        }
        boolean enterTapped = pick.enterTapped();
        boolean destinationToHand = queue.destinationToHand();
        boolean reveals = reveals(queue);
        String destinationText = destinationToHand
                ? " into your hand"
                : enterTapped ? " onto the battlefield tapped" : " onto the battlefield";
        if (!queue.optionalSearch()) {
            LibrarySearchTriggerHelper.recordSearchAndQueueTriggers(gameData, playerId);
        }
        String prompt = (queue.optionalSearch() ? "You may search" : "Search")
                + " your library for up to " + count + " " + described
                + (count == 1 ? "" : "s")
                + (reveals ? " to reveal and put them" : " and put them")
                + destinationText + " (" + count + " remaining).";

        LibrarySearchParams searchParams = LibrarySearchParams.builder(playerId, count <= 0 ? new ArrayList<>() : new ArrayList<>(matching))
                        .remainingCount(count)
                        .canFailToFind(true)
                        .reveals(reveals)
                        .destination(destinationToHand
                                ? LibrarySearchDestination.HAND
                                : enterTapped
                                ? LibrarySearchDestination.BATTLEFIELD_TAPPED
                                : LibrarySearchDestination.BATTLEFIELD)
                        .filterPredicate(new CardAnyOfPredicate(List.of(
                                queue.filter(), new CardHasAllCardNamesPredicate())))
                        .shuffleAfterSelection(!shuffleAfterQueue)
                        .followUp(followUp)
                        .build();
        if (choosesWhetherToSearch) {
            librarySearchSupport.sendLibrarySearchToPlayer(gameData, playerId, searchParams, prompt, true,
                    playerName + " may search their library.");
        } else {
            librarySearchSupport.sendLibrarySearchToPlayer(gameData, playerId, searchParams, prompt, true);
        }
        return true;
    }

    private void shuffleAfterQueue(GameData gameData,
                                   LibrarySearchFollowUp.BasicLandSearchQueue queue) {
        if (!queue.shuffleAfterQueue() || queue.leavesLibrariesUnshuffled()) {
            return;
        }
        for (UUID playerId : queue.searchedPlayerIds()) {
            if (gameData.playerDecks.get(playerId) == null) {
                continue;
            }
            LibraryShuffleHelper.shuffleLibrary(gameData, playerId);
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(playerId) + "'s library is shuffled."));
        }
    }

    /**
     * Starts the forced "keep five lands, sacrifice the rest" choices. Every queued player controls
     * at least six lands, so each of them always gets a choice; returns false only when nobody has
     * to sacrifice.
     */
    private boolean beginSacrifices(GameData gameData, LibrarySearchFollowUp.BasicLandSearchQueue queue) {
        if (queue.sacrifices().isEmpty()) {
            return false;
        }
        destructionSupport.beginNextForcedSacrificeFromQueue(gameData, queue.sacrifices(), List.of());
        return true;
    }
}
