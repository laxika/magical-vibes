package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantPlayCardsFromHandUntilNextTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlayersCantPlayCardsFromHandUntilNextTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PlayersCantPlayCardsFromHandUntilNextTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        Set<UUID> affectedPlayers = gameData.playersCantPlayCardsFromHandUntilControllerNextTurn
                .computeIfAbsent(controllerId, ignored -> ConcurrentHashMap.newKeySet());
        affectedPlayers.addAll(gameData.orderedPlayerIds);
        gameLogService.append(gameData, GameLog.text(
                "Players can't play cards from their hands until "
                        + gameData.playerIdToName.get(controllerId) + "'s next turn."));
    }
}
