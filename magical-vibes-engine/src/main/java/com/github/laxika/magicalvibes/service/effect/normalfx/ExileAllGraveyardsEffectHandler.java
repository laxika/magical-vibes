package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAllGraveyardsEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileAllGraveyardsEffectHandler implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileAllGraveyardsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        int totalExiled = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null || graveyard.isEmpty()) continue;
            for (Card card : graveyard) {
                exileService.exileCard(gameData, playerId, card);
                totalExiled++;
            }
            graveyard.clear();
        }

        if (totalExiled > 0) {
            String logEntry = "All graveyards are exiled (" + totalExiled + " card"
                    + (totalExiled != 1 ? "s" : "") + ").";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - All graveyards exiled ({} cards) by {}",
                    gameData.id, totalExiled, entry.getCard().getName());
        } else {
            String logEntry = "All graveyards are already empty.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - All graveyards already empty when {} resolved",
                    gameData.id, entry.getCard().getName());
        }
    }
}
