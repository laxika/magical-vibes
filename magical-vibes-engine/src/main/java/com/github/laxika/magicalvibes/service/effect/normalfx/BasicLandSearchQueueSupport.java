package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardHasAllCardNamesPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.library.LibrarySearchTriggerHelper;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Drives an each-player basic-land search: first the APNAP queue of per-player "you may search your
 * library for up to X basic land cards and put them onto the battlefield" picks, then any forced
 * land sacrifices held on the same {@link LibrarySearchFollowUp.BasicLandSearchQueue}. The queue is
 * advanced from the effect handler and re-entered by the library-search input handler after every
 * pick resolves. Used by Natural Balance (searches then sacrifices) and Veteran Explorer (searches
 * only).
 */
@Component
@RequiredArgsConstructor
public class BasicLandSearchQueueSupport {

    private static final CardPredicate BASIC_LAND = new CardAllOfPredicate(List.of(
            new CardTypePredicate(CardType.LAND), new CardSupertypePredicate(CardSupertype.BASIC)));
    private static final CardPredicate BASIC_LAND_SEARCH = new CardAnyOfPredicate(List.of(
            BASIC_LAND, new CardHasAllCardNamesPredicate()));

    private final LibrarySearchSupport librarySearchSupport;
    private final DestructionSupport destructionSupport;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final BattlefieldEntryService battlefieldEntryService;

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
            if (!librarySearchSupport.isSearchPrevented(gameData, pick.playerId())) {
                nextQueue = nextQueue.withSearchedPlayer(pick.playerId());
            }
            if (startSearch(gameData, pick, followUp.withBasicLandSearchQueue(nextQueue))) {
                return true;
            }
            queue = nextQueue;
        }

        placeSelectedLands(gameData, queue);
        shuffleAfterQueue(gameData, queue);
        return beginSacrifices(gameData, queue);
    }

    private void placeSelectedLands(GameData gameData, LibrarySearchFollowUp.BasicLandSearchQueue queue) {
        if (queue.selectedLands().isEmpty()) return;
        List<Map.Entry<UUID, Permanent>> prepared = new ArrayList<>();
        for (LibrarySearchFollowUp.DeferredBasicLand selected : queue.selectedLands()) {
            if (gameQueryService.isCardBlockedFromEnteringFromZone(gameData, selected.card(), Zone.LIBRARY)) {
                continue;
            }
            List<Card> library = gameData.playerDecks.get(selected.libraryOwnerId());
            if (library == null || !library.remove(selected.card())) {
                continue;
            }
            Permanent permanent = new Permanent(selected.card(), Zone.LIBRARY);
            if (selected.enterTapped()) permanent.tap();
            prepared.add(Map.entry(selected.battlefieldControllerId(), permanent));
        }
        var enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        List<Permanent> entering = prepared.stream().map(Map.Entry::getValue).toList();
        for (Map.Entry<UUID, Permanent> selected : prepared) {
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, selected.getKey(), selected.getValue(),
                    enterTappedTypes, entering);
            gameLogService.append(gameData, GameLog.entersBattlefieldUnder(selected.getValue().getCard(),
                    gameData.playerIdToName.get(selected.getKey())));
        }
        for (Map.Entry<UUID, Permanent> selected : prepared) {
            UUID actualControllerId = gameQueryService.findPermanentController(gameData, selected.getValue().getId());
            if (actualControllerId != null) {
                battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, actualControllerId,
                        selected.getValue().getCard(), null, false);
            }
        }
    }

    private boolean startSearch(GameData gameData, LibrarySearchFollowUp.BasicLandsPick pick,
            LibrarySearchFollowUp followUp) {
        UUID playerId = pick.playerId();
        if (librarySearchSupport.isSearchPrevented(gameData, playerId)) {
            return false;
        }

        boolean shuffleAfterQueue = followUp.basicLandSearchQueue().shuffleAfterQueue();
        String playerName = gameData.playerIdToName.get(playerId);
        List<Card> deck = gameData.playerDecks.get(playerId);
        if (deck == null || deck.isEmpty()) {
            LibrarySearchTriggerHelper.recordSearchAndQueueTriggers(gameData, gameLogService, playerId);
            if (!shuffleAfterQueue && deck != null) LibraryShuffleHelper.shuffleLibrary(gameData, playerId);
            gameLogService.append(gameData,
                    GameLog.text(playerName + " searches their library but it is empty."
                            + (shuffleAfterQueue ? "" : " Library is shuffled.")));
            return false;
        }

        List<Card> basicLands = deck.stream()
                .filter(card -> card.hasAllCardNames()
                        || (card.hasType(CardType.LAND)
                        && gameQueryService.cardHasSupertype(card, CardSupertype.BASIC, gameData, playerId)))
                .toList();
        if (basicLands.isEmpty()) {
            LibrarySearchTriggerHelper.recordSearchAndQueueTriggers(gameData, gameLogService, playerId);
            if (!shuffleAfterQueue) LibraryShuffleHelper.shuffleLibrary(gameData, playerId);
            gameLogService.append(gameData, GameLog.text(
                    playerName + " searches their library but finds no basic land cards."
                            + (shuffleAfterQueue ? "" : " Library is shuffled.")));
            return false;
        }

        int count = pick.count();
        if (count <= 0) {
            LibrarySearchTriggerHelper.recordSearchAndQueueTriggers(gameData, gameLogService, playerId);
            if (!shuffleAfterQueue) LibraryShuffleHelper.shuffleLibrary(gameData, playerId);
            gameLogService.append(gameData, GameLog.text(
                    playerName + " searches their library for up to zero basic land cards."
                            + (shuffleAfterQueue ? "" : " Library is shuffled.")));
            return false;
        }
        boolean enterTapped = pick.enterTapped();
        boolean destinationToHand = followUp.basicLandSearchQueue().destinationToHand();
        String destinationText = destinationToHand
                ? " into your hand"
                : enterTapped ? " onto the battlefield tapped" : " onto the battlefield";
        if (!followUp.basicLandSearchQueue().optionalSearch()) {
            LibrarySearchTriggerHelper.recordSearchAndQueueTriggers(gameData, gameLogService, playerId);
        }
        String prompt = (followUp.basicLandSearchQueue().optionalSearch() ? "You may search" : "Search")
                + " your library for up to " + count + " basic land card"
                + (count == 1 ? "" : "s")
                + (destinationToHand ? " to reveal and put them" : " and put them")
                + destinationText + " (" + count + " remaining).";

        librarySearchSupport.sendLibrarySearchToPlayer(gameData, playerId,
                LibrarySearchParams.builder(playerId, new ArrayList<>(basicLands))
                        .remainingCount(count)
                        .canFailToFind(true)
                        .reveals(destinationToHand)
                        .destination(destinationToHand
                                ? LibrarySearchDestination.HAND
                                : enterTapped
                                ? LibrarySearchDestination.BATTLEFIELD_TAPPED
                                : LibrarySearchDestination.BATTLEFIELD)
                        .filterPredicate(BASIC_LAND_SEARCH)
                        .shuffleAfterSelection(!shuffleAfterQueue)
                        .followUp(followUp)
                        .build(), prompt, true);
        return true;
    }

    private void shuffleAfterQueue(GameData gameData,
                                   LibrarySearchFollowUp.BasicLandSearchQueue queue) {
        if (!queue.shuffleAfterQueue()) {
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
