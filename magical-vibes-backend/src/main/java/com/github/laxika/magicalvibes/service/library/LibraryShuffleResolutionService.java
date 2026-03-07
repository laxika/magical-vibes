package com.github.laxika.magicalvibes.service.library;

import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.effect.HandlesEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.ShuffleGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
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

    /**
     * Shuffles the spell's card into its owner's library instead of going to the graveyard.
     * Used by cards like Red Sun's Zenith.
     */
    @HandlesEffect(ShuffleIntoLibraryEffect.class)
    void resolveShuffleIntoLibrary(GameData gameData, StackEntry entry) {
        List<Card> deck = gameData.playerDecks.get(entry.getControllerId());
        deck.add(entry.getCard());
        Collections.shuffle(deck);

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
            Collections.shuffle(deck);
            return;
        }

        int count = graveyard.size();
        deck.addAll(graveyard);
        graveyard.clear();
        Collections.shuffle(deck);

        String logEntry = playerName + " shuffles their graveyard (" + pluralCards(count) + ") into their library.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} shuffles graveyard ({} cards) into library", gameData.id, playerName, count);
    }

    private static String pluralCards(int count) {
        return count + " card" + (count != 1 ? "s" : "");
    }
}
