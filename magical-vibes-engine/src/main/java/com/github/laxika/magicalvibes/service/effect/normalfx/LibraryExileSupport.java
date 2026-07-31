package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Shared helpers for moving cards from a player's library to exile. Used by the cumulative-upkeep
 * "Exile the top card of your library" cost path and by {@code ExileControllerLibraryEffect}
 * (Thought Lash).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LibraryExileSupport {

    private final ExileService exileService;
    private final GameLogService gameLogService;

    /** True when {@code playerId}'s library holds at least {@code count} cards. */
    public boolean hasAtLeast(GameData gameData, UUID playerId, int count) {
        List<Card> deck = gameData.playerDecks.get(playerId);
        return deck != null && deck.size() >= count;
    }

    /** Exiles the top {@code count} cards of {@code playerId}'s library, stopping if it empties. */
    public void exileTopCards(GameData gameData, UUID playerId, int count) {
        List<Card> deck = gameData.playerDecks.get(playerId);
        String playerName = gameData.playerIdToName.get(playerId);
        for (int i = 0; i < count && deck != null && !deck.isEmpty(); i++) {
            Card topCard = deck.removeFirst();
            exileService.exileCard(gameData, playerId, topCard);
            gameLogService.append(gameData, GameLog.textCardText(
                    playerName + " exiles ", topCard, " from the top of their library."));
        }
        log.info("Game {} - {} exiles up to {} cards from their library top", gameData.id, playerName, count);
    }

    /** Exiles every card in {@code playerId}'s library (Thought Lash). */
    public void exileEntireLibrary(GameData gameData, UUID playerId) {
        List<Card> deck = gameData.playerDecks.get(playerId);
        String playerName = gameData.playerIdToName.get(playerId);
        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(playerName + "'s library is already empty."));
            return;
        }
        int exiled = deck.size();
        List<Card> cards = List.copyOf(deck);
        deck.clear();
        cards.forEach(card -> exileService.exileCard(gameData, playerId, card));
        gameLogService.append(gameData, GameLog.text(
                playerName + " exiles all " + exiled + " cards from their library."));
        log.info("Game {} - {} exiles their entire library ({} cards)", gameData.id, playerName, exiled);
    }
}
