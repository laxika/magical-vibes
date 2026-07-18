package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetCardsFromGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShuffleTargetCardsFromGraveyardIntoLibraryEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final GameQueryService gameQueryService;
    private final GraveyardService graveyardService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ShuffleTargetCardsFromGraveyardIntoLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        List<UUID> targetCardIds = entry.getTargetCardIds();
        String playerName = gameData.playerIdToName.get(targetPlayerId);

        if (targetPlayerId == null || targetCardIds == null || targetCardIds.isEmpty()) {
            // No targets — just shuffle the target player's library if we have a target player
            if (targetPlayerId != null) {
                LibraryShuffleHelper.shuffleLibrary(gameData, targetPlayerId);
                gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(playerName + " shuffles their library (", entry.getCard(), ")."));
            }
            return;
        }

        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        List<Card> graveyard = gameData.playerGraveyards.get(targetPlayerId);
        List<String> movedNames = new ArrayList<>();

        graveyardService.beginGraveyardLeaveBatch(gameData);
        try {
            for (UUID cardId : targetCardIds) {
                Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
                if (card != null && graveyard != null && graveyard.removeIf(c -> c.getId().equals(cardId))) {
                    deck.add(card);
                    movedNames.add(card.getName());
                    graveyardService.notifyCardsLeftGraveyard(gameData, targetPlayerId);
                }
            }
        } finally {
            graveyardService.endGraveyardLeaveBatch(gameData);
        }

        LibraryShuffleHelper.shuffleLibrary(gameData, targetPlayerId);

        if (!movedNames.isEmpty()) {
            String logEntry = playerName + " shuffles " + String.join(", ", movedNames)
                    + " from graveyard into their library.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} shuffles {} card(s) from graveyard into library",
                    gameData.id, playerName, movedNames.size());
        } else {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(playerName + " shuffles their library (", entry.getCard(), ")."));
        }
    }
}
