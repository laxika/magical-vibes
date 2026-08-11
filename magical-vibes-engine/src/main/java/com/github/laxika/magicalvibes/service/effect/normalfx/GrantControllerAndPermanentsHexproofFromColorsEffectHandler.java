package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantControllerAndPermanentsHexproofFromColorsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class GrantControllerAndPermanentsHexproofFromColorsEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantControllerAndPermanentsHexproofFromColorsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (GrantControllerAndPermanentsHexproofFromColorsEffect) effect;
        UUID controllerId = entry.getControllerId();
        gameData.playerHexproofFromColorsThisTurn
                .computeIfAbsent(controllerId, ignored -> ConcurrentHashMap.newKeySet())
                .addAll(e.colors());

        for (Permanent permanent : gameData.playerBattlefields.getOrDefault(controllerId, java.util.List.of())) {
            gameData.permanentHexproofFromColorsThisTurn
                    .computeIfAbsent(permanent.getId(), ignored -> ConcurrentHashMap.newKeySet())
                    .addAll(e.colors());
        }

        String colorNames = e.colors().stream()
                .map(color -> color.name().toLowerCase())
                .sorted()
                .reduce((first, second) -> first + " or " + second)
                .orElse("");
        gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(controllerId)
                + " and permanents they control gain hexproof from " + colorNames + " until end of turn."));
    }
}
