package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAllOpponentsGraveyardsEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import java.util.List;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileAllOpponentsGraveyardsEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileAllOpponentsGraveyardsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {

        UUID controllerId = entry.getControllerId();

        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(controllerId)) continue;

            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard.isEmpty()) continue;

            int count = graveyard.size();
            for (Card card : graveyard) {
                gameData.addToExile(playerId, card);
            }
            graveyard.clear();

            String playerName = gameData.playerIdToName.get(playerId);
            String logEntry = playerName + "'s graveyard is exiled (" + count + " card" + (count != 1 ? "s" : "") + ").";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {}'s graveyard ({} cards) exiled by ExileAllOpponentsGraveyardsEffect",
                    gameData.id, playerName, count);
        }
    }
}
