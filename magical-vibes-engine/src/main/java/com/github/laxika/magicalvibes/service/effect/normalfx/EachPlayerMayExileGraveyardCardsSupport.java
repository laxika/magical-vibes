package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Completes a player's selected graveyard exile. */
@Component
@RequiredArgsConstructor
public class EachPlayerMayExileGraveyardCardsSupport {

    private final GraveyardReturnSupport graveyardReturnSupport;
    private final GameLogService gameLogService;

    public void completeSelection(GameData gameData, UUID playerId, List<UUID> cardIds) {
        List<Card> graveyard = gameData.playerGraveyards.get(playerId);
        List<String> exiledNames = new ArrayList<>();
        if (graveyard != null) {
            for (UUID cardId : cardIds) {
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
}
