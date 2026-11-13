package com.github.laxika.magicalvibes.service.library;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.rules.LibraryEvent;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Centralized library shuffle utility that shuffles a player's library and emits the shuffle as a rules event, which
 * the shuffle triggers trigger on.
 */
public final class LibraryShuffleHelper {

    private LibraryShuffleHelper() {}

    public static void shuffleLibrary(GameData gameData, UUID playerId) {
        gameData.restoreBombardmentLibraryCards(playerId);
        gameData.libraryTopCardFreePlayPermissionsUntilEndOfTurn.remove(playerId);
        gameData.pendingCommanderZoneMoves.replaceAll(move -> move.ownerId().equals(playerId)
                && move.destination() == com.github.laxika.magicalvibes.model.Zone.LIBRARY ? move.shuffled() : move);
        List<Card> deck = gameData.playerDecks.get(playerId);
        Collections.shuffle(deck);
        emitShuffle(gameData, playerId);
    }

    /**
     * Shuffles the library recorded in {@code libraryToShuffleAfterFreeCast}, if any. Called once the
     * free-cast spell has been put on the stack, so shuffle triggers go on the stack above it.
     */
    public static void shuffleLibraryPendingAfterFreeCast(GameData gameData) {
        UUID playerId = gameData.libraryToShuffleAfterFreeCast;
        if (playerId == null) {
            return;
        }
        gameData.libraryToShuffleAfterFreeCast = null;
        shuffleLibrary(gameData, playerId);
    }

    /**
     * Emits the shuffle of {@code shufflingPlayerId}'s library as a rules event, with the controller of the spell or
     * ability resolving now as the one that caused it: the "whenever a player shuffles their library" abilities trigger
     * on it (CR 603.2), and the trigger pipeline hands them to the stack door
     * ({@code TriggerCollectionService.checkLibraryShuffleTriggers}) as the event's batch closes.
     */
    private static void emitShuffle(GameData gameData, UUID shufflingPlayerId) {
        UUID causingControllerId = gameData.currentlyResolvingControllerId != null
                ? gameData.currentlyResolvingControllerId
                : gameData.pendingEffectResolutionEntry != null
                ? gameData.pendingEffectResolutionEntry.getControllerId()
                : null;
        gameData.rulesEvents.emit(LibraryEvent.shuffled(shufflingPlayerId, causingControllerId));
    }
}
