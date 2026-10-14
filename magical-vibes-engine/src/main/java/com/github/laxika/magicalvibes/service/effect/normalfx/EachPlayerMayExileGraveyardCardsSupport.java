package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Completes a player's selected graveyard exile. */
@Component
@RequiredArgsConstructor
public class EachPlayerMayExileGraveyardCardsSupport {

    private final GraveyardReturnSupport graveyardReturnSupport;
    private final GameLogService gameLogService;

    public void completeSelection(GameData gameData, UUID playerId, List<UUID> cardIds) {
        List<Card> graveyard = gameData.playerGraveyards.get(playerId);
        List<UUID> chosen = new ArrayList<>();
        if (graveyard != null) {
            for (UUID cardId : cardIds) {
                boolean inGraveyard = graveyard.stream().anyMatch(candidate -> candidate.getId().equals(cardId));
                if (inGraveyard && !chosen.contains(cardId)) {
                    chosen.add(cardId);
                }
            }
        }
        gameData.eachPlayerMayExileGraveyardCards.recordChoice(playerId, chosen);
    }

    /** Exiles every card chosen by every player at once, after all players have made their choices. */
    public void exileChosenCardsSimultaneously(GameData gameData) {
        var state = gameData.eachPlayerMayExileGraveyardCards;
        for (Map.Entry<UUID, List<UUID>> choice : state.chosenCardIds.entrySet()) {
            UUID playerId = choice.getKey();
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            List<String> exiledNames = new ArrayList<>();
            if (graveyard != null) {
                for (UUID cardId : choice.getValue()) {
                    Card card = graveyard.stream()
                            .filter(candidate -> candidate.getId().equals(cardId))
                            .findFirst()
                            .orElse(null);
                    if (card != null && graveyardReturnSupport.exileCardFromAnyGraveyard(gameData, cardId, card)) {
                        exiledNames.add(card.getName());
                    }
                }
            }

            String playerName = gameData.playerIdToName.get(playerId);
            if (!exiledNames.isEmpty()) {
                gameLogService.append(gameData, GameLog.text(
                        playerName + " exiles " + String.join(", ", exiledNames) + " from their graveyard."));
            }
        }
        state.chosenCardIds.clear();
    }
}
