package com.github.laxika.magicalvibes.service.library;

import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.HandlesEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.ShuffleGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleSelfAndGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Resolves shuffle-related library effects during stack resolution.
 *
 * <p>Handles effects that shuffle cards into a player's library, including
 * shuffling the spell itself back in and shuffling a player's graveyard
 * into their library.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LibraryShuffleResolutionService {

    private final GameBroadcastService gameBroadcastService;
    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;

    /**
     * Shuffles the spell's card into its owner's library instead of going to the graveyard.
     * Used by cards like Red Sun's Zenith.
     */
    @HandlesEffect(ShuffleIntoLibraryEffect.class)
    void resolveShuffleIntoLibrary(GameData gameData, StackEntry entry) {
        List<Card> deck = gameData.playerDecks.get(entry.getControllerId());
        deck.add(entry.getCard());
        LibraryShuffleHelper.shuffleLibrary(gameData, entry.getControllerId());

        String shuffleLog = entry.getCard().getName() + " is shuffled into its owner's library.";
        gameBroadcastService.logAndBroadcast(gameData, shuffleLog);
    }

    /**
     * Shuffles the target player's entire graveyard into their library.
     * Used by cards like Reminisce.
     */
    @HandlesEffect(ShuffleGraveyardIntoLibraryEffect.class)
    void resolveShuffleGraveyardIntoLibrary(GameData gameData, StackEntry entry) {
        UUID targetPlayerId = entry.getTargetPermanentId();
        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        List<Card> graveyard = gameData.playerGraveyards.get(targetPlayerId);
        String playerName = gameData.playerIdToName.get(targetPlayerId);

        if (graveyard.isEmpty()) {
            String logEntry = playerName + "'s graveyard is empty. Library is shuffled.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            LibraryShuffleHelper.shuffleLibrary(gameData, targetPlayerId);
            return;
        }

        int count = graveyard.size();
        deck.addAll(graveyard);
        graveyard.clear();
        LibraryShuffleHelper.shuffleLibrary(gameData, targetPlayerId);

        String logEntry = playerName + " shuffles their graveyard (" + pluralCards(count) + ") into their library.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} shuffles graveyard ({} cards) into library", gameData.id, playerName, count);
    }

    /**
     * Removes the source permanent from the battlefield and shuffles it along with the
     * controller's entire graveyard into their library. If the source permanent has already
     * left the battlefield, only the graveyard is shuffled in.
     * Used by cards like Elixir of Immortality.
     */
    @HandlesEffect(ShuffleSelfAndGraveyardIntoLibraryEffect.class)
    void resolveShuffleSelfAndGraveyardIntoLibrary(GameData gameData, StackEntry entry) {
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);

        // Move source permanent from battlefield to library (if still there)
        boolean selfShuffled = false;
        if (entry.getSourcePermanentId() != null) {
            Permanent self = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
            if (self != null) {
                permanentRemovalService.removePermanentToLibraryBottom(gameData, self);
                permanentRemovalService.removeOrphanedAuras(gameData);
                selfShuffled = true;
            }
        }

        // Move all graveyard cards into library
        int graveyardCount = graveyard.size();
        if (!graveyard.isEmpty()) {
            deck.addAll(graveyard);
            graveyard.clear();
        }

        // Shuffle the library
        LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);

        // Log
        if (selfShuffled && graveyardCount > 0) {
            String logEntry = playerName + " shuffles " + entry.getCard().getName()
                    + " and their graveyard (" + pluralCards(graveyardCount) + ") into their library.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
        } else if (selfShuffled) {
            String logEntry = playerName + " shuffles " + entry.getCard().getName() + " into their library.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
        } else if (graveyardCount > 0) {
            String logEntry = playerName + " shuffles their graveyard (" + pluralCards(graveyardCount) + ") into their library.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
        } else {
            String logEntry = playerName + " shuffles their library.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
        }

        log.info("Game {} - {} shuffles self={} + graveyard ({} cards) into library",
                gameData.id, playerName, selfShuffled, graveyardCount);
    }

    private static String pluralCards(int count) {
        return count + " card" + (count != 1 ? "s" : "");
    }
}
