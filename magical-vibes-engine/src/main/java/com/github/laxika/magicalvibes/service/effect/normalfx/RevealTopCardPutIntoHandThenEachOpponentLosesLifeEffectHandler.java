package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardPutIntoHandThenEachOpponentLosesLifeEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevealTopCardPutIntoHandThenEachOpponentLosesLifeEffectHandler implements NormalEffectHandlerBean {

    private final LifeSupport lifeSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardPutIntoHandThenEachOpponentLosesLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();

        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(playerName + "'s library is empty (" + sourceName + ")."));
            return;
        }

        Card topCard = deck.removeFirst();
        int manaValue = topCard.getManaValue();
        gameLogService.append(gameData, GameLog.builder()
                .text(playerName + " reveals ").card(topCard)
                .text(" (mana value " + manaValue + ") from the top of their library.")
                .build());
        gameData.addCardToHand(controllerId, topCard);

        if (manaValue > 0) {
            for (UUID playerId : gameData.orderedPlayerIds) {
                if (!playerId.equals(controllerId)) {
                    lifeSupport.applyLifeLoss(gameData, playerId, manaValue, sourceName);
                }
            }
        }

        log.info("Game {} - {} reveals {} (MV {}) via {}", gameData.id, playerName,
                topCard.getName(), manaValue, sourceName);
    }
}
