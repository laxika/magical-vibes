package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantControllerCreaturesCantBeTargetedByColorsEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GrantControllerCreaturesCantBeTargetedByColorsEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantControllerCreaturesCantBeTargetedByColorsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (GrantControllerCreaturesCantBeTargetedByColorsEffect) effect;
        UUID controllerId = entry.getControllerId();
        gameData.playerCreaturesCantBeTargetedByColorsThisTurn
                .computeIfAbsent(controllerId, k -> ConcurrentHashMap.newKeySet())
                .addAll(e.colors());

        String colorNames = e.colors().stream()
                .map(c -> c.name().toLowerCase())
                .sorted()
                .reduce((a, b) -> a + " or " + b)
                .orElse("");
        String logEntry = "Creatures " + gameData.playerIdToName.get(controllerId) + " controls can't be the targets of " + colorNames + " spells this turn.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
    }
}
