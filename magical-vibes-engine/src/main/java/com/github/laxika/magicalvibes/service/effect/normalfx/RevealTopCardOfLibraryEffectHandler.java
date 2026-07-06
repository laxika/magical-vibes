package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevealTopCardOfLibraryEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardOfLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {

        UUID targetPlayerId = entry.getTargetId();
        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        String playerName = gameData.playerIdToName.get(targetPlayerId);

        if (deck.isEmpty()) {
            String logEntry = playerName + "'s library is empty.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
        } else {
            Card topCard = deck.getFirst();
            String logEntry = playerName + " reveals " + topCard.getName() + " from the top of their library.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
        }

        log.info("Game {} - {} reveals top card of library", gameData.id, playerName);
    
    }
}
