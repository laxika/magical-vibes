package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantControllerKeywordUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class GrantControllerKeywordUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantControllerKeywordUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var grant = (GrantControllerKeywordUntilEndOfTurnEffect) effect;
        UUID controllerId = entry.getControllerId();
        gameData.playerKeywordsUntilEndOfTurn
                .computeIfAbsent(controllerId, ignored -> ConcurrentHashMap.newKeySet())
                .add(grant.keyword());
        gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(controllerId)
                + " gains " + grant.keyword().name().toLowerCase() + " until end of turn."));
    }
}
