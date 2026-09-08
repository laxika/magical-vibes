package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerRevealsTopCardLosesLifeEqualToManaValueThenToHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EachPlayerRevealsTopCardLosesLifeEqualToManaValueThenToHandEffectHandler
        implements NormalEffectHandlerBean {

    private final LifeSupport lifeSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerRevealsTopCardLosesLifeEqualToManaValueThenToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        String sourceName = entry.getCard().getName();

        for (UUID playerId : apnapOrder(gameData)) {
            List<Card> deck = gameData.playerDecks.get(playerId);
            String playerName = gameData.playerIdToName.get(playerId);

            if (deck == null || deck.isEmpty()) {
                gameLogService.append(gameData,
                        GameLog.text(playerName + "'s library is empty (" + sourceName + ")."));
                continue;
            }

            Card topCard = deck.removeFirst();
            int manaValue = topCard.getManaValue();

            gameLogService.append(gameData, GameLog.builder()
                    .text(playerName + " reveals ")
                    .card(topCard)
                    .text(" (mana value " + manaValue + ") from the top of their library (" + sourceName + ").")
                    .build());

            if (manaValue > 0) {
                lifeSupport.applyLifeLoss(gameData, playerId, manaValue, sourceName);
            }

            gameData.addCardToHand(playerId, topCard);

            log.info("Game {} - {} reveals {} (MV {}) and puts it into hand via {}",
                    gameData.id, playerName, topCard.getName(), manaValue, sourceName);
        }
    }

    private List<UUID> apnapOrder(GameData gameData) {
        List<UUID> order = new ArrayList<>();
        order.add(gameData.activePlayerId);
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(gameData.activePlayerId)) {
                order.add(playerId);
            }
        }
        return order;
    }
}
