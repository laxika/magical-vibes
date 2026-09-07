package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.OtherPlayersCantPlayFromGraveyardsThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OtherPlayersCantPlayFromGraveyardsThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return OtherPlayersCantPlayFromGraveyardsThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        gameData.playersCantPlayFromGraveyardsThisTurn.addAll(gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(controllerId))
                .toList());

        gameLogService.append(gameData, GameLog.text("Players other than "
                + gameData.playerIdToName.get(controllerId)
                + " can't play cards from their graveyards this turn."));
        log.info("Game {} - players other than {} can't play cards from their graveyards this turn",
                gameData.id, gameData.playerIdToName.get(controllerId));
    }
}
