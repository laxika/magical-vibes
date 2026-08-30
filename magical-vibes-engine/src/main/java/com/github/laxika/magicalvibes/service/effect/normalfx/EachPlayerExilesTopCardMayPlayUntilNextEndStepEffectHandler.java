package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerExilesTopCardMayPlayUntilNextEndStepEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EachPlayerExilesTopCardMayPlayUntilNextEndStepEffectHandler
        implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final ExileSupport exileSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerExilesTopCardMayPlayUntilNextEndStepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID endStepPlayerId = entry.getControllerId();
        String sourceName = entry.getCard().getName();

        for (UUID playerId : gameData.orderedPlayerIds) {
            var library = gameData.playerDecks.get(playerId);
            if (library == null || library.isEmpty()) {
                continue;
            }

            Card topCard = library.removeFirst();
            exileService.exileCard(gameData, playerId, topCard);
            exileSupport.grantPlayUntilNextEndStepOfPlayer(
                    gameData, topCard.getId(), playerId, endStepPlayerId);
            gameLogService.append(gameData, GameLog.builder()
                    .text(gameData.playerIdToName.get(playerId) + " exiles ")
                    .card(topCard)
                    .text(" from the top of their library (may play until "
                            + sourceName + "'s next end step).")
                    .build());
        }

        log.info("Game {} - {} exiles the top card of each player's library",
                gameData.id, sourceName);
    }
}
