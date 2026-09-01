package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.RevokeExilePlayPermissionAtNextUpkeep;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerExilesTopCardsMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EachPlayerExilesTopCardsMayPlayUntilNextTurnEffectHandler implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerExilesTopCardsMayPlayUntilNextTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        int count = ((EachPlayerExilesTopCardsMayPlayUntilNextTurnEffect) effect).count();
        if (count <= 0) {
            return;
        }

        UUID expiryControllerId = entry.getControllerId();
        String sourceName = entry.getCard().getName();
        for (UUID playerId : gameData.orderedPlayerIds) {
            var library = gameData.playerDecks.get(playerId);
            if (library == null || library.isEmpty()) {
                continue;
            }

            int cardsToExile = Math.min(count, library.size());
            for (int i = 0; i < cardsToExile; i++) {
                Card topCard = library.removeFirst();
                exileService.exileCard(gameData, playerId, topCard);
                gameData.exilePlayPermissions.put(topCard.getId(), playerId);
                gameData.queueDelayedAction(new RevokeExilePlayPermissionAtNextUpkeep(
                        expiryControllerId, topCard.getId(), entry.getCard()));
                gameLogService.append(gameData, GameLog.builder()
                        .text(gameData.playerIdToName.get(playerId) + " exiles ")
                        .card(topCard)
                        .text(" from the top of their library (may play until "
                                + sourceName + "'s next turn).")
                        .build());
            }
        }

        log.info("Game {} - {} exiles up to {} cards from the top of each player's library",
                gameData.id, sourceName, count);
    }
}
