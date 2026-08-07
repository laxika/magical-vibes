package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.ArrayList;
import java.util.List;
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

    private final LibrarySearchSupport librarySearchSupport;
    private final DestructionSupport destructionSupport;
    private final GameLogService gameLogService;

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
            if (startSearch(gameData, pick, followUp.withBasicLandSearchQueue(queue.withRemainingPicks(remaining)))) {
                return true;
            }
        }

        return beginSacrifices(gameData, queue);
    }

    private boolean startSearch(GameData gameData, LibrarySearchFollowUp.BasicLandsPick pick,
            LibrarySearchFollowUp followUp) {
        UUID playerId = pick.playerId();
        if (librarySearchSupport.isSearchPrevented(gameData, playerId)) {
            return false;
        }

        String playerName = gameData.playerIdToName.get(playerId);
        List<Card> deck = gameData.playerDecks.get(playerId);
        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.text(playerName + " searches their library but it is empty. Library is shuffled."));
            return false;
        }

        List<Card> basicLands = deck.stream()
                .filter(card -> card.hasType(CardType.LAND) && card.getSupertypes().contains(CardSupertype.BASIC))
                .toList();
        if (basicLands.isEmpty()) {
            LibraryShuffleHelper.shuffleLibrary(gameData, playerId);
            gameLogService.append(gameData, GameLog.text(
                    playerName + " searches their library but finds no basic land cards. Library is shuffled."));
            return false;
        }

        int count = pick.count();
        String prompt = "You may search your library for up to " + count + " basic land card"
                + (count == 1 ? "" : "s") + " and put them onto the battlefield (" + count + " remaining).";

        librarySearchSupport.sendLibrarySearchToPlayer(gameData, playerId,
                LibrarySearchParams.builder(playerId, new ArrayList<>(basicLands))
                        .remainingCount(count)
                        .canFailToFind(true)
                        .destination(LibrarySearchDestination.BATTLEFIELD)
                        .filterPredicate(BASIC_LAND)
                        .followUp(followUp)
                        .build(), prompt, true);
        return true;
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
