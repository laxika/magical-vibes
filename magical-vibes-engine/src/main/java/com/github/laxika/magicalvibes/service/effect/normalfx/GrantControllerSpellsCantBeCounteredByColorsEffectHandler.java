package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantControllerSpellsCantBeCounteredByColorsEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GrantControllerSpellsCantBeCounteredByColorsEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantControllerSpellsCantBeCounteredByColorsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (GrantControllerSpellsCantBeCounteredByColorsEffect) effect;
        UUID controllerId = entry.getControllerId();
        gameData.playerSpellsCantBeCounteredByColorsThisTurn
                .computeIfAbsent(controllerId, k -> ConcurrentHashMap.newKeySet())
                .addAll(e.colors());

        String colorNames = e.colors().stream()
                .map(c -> c.name().toLowerCase())
                .sorted()
                .reduce((a, b) -> a + " or " + b)
                .orElse("");
        String logEntry = "Spells " + gameData.playerIdToName.get(controllerId) + " controls can't be countered by " + colorNames + " spells this turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
    }
}
