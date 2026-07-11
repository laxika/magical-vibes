package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardPutIntoHandAndLoseLifeEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevealTopCardPutIntoHandAndLoseLifeEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardPutIntoHandAndLoseLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {

        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();

        if (deck.isEmpty()) {
            String logEntry = playerName + "'s library is empty (" + sourceName + ").";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        Card topCard = deck.removeFirst();
        int manaValue = topCard.getManaValue();

        // Reveal the card
        gameBroadcastService.logAndBroadcast(gameData,
                playerName + " reveals " + topCard.getName() + " (mana value " + manaValue + ") from the top of their library.");

        // Put it into hand
        gameData.addCardToHand(controllerId, topCard);

        // Lose life equal to mana value
        if (manaValue > 0) {
            if (gameQueryService.canPlayerLifeChange(gameData, controllerId)) {
                int currentLife = gameData.getLife(controllerId);
                gameData.playerLifeTotals.put(controllerId, currentLife - manaValue);
                gameBroadcastService.logAndBroadcast(gameData,
                        playerName + " puts " + topCard.getName() + " into their hand and loses " + manaValue + " life (" + sourceName + ").");
            } else {
                gameBroadcastService.logAndBroadcast(gameData,
                        playerName + " puts " + topCard.getName() + " into their hand. " + playerName + "'s life total can't change (" + sourceName + ").");
            }
        } else {
            gameBroadcastService.logAndBroadcast(gameData,
                    playerName + " puts " + topCard.getName() + " into their hand (" + sourceName + ").");
        }

        log.info("Game {} - {} reveals {} (MV {}) from Dark Tutelage", gameData.id, playerName, topCard.getName(), manaValue);
    
    }
}
