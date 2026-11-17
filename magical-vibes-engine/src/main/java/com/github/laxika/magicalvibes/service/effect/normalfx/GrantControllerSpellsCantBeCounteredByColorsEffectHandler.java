package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantControllerSpellsCantBeCounteredByColorsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GrantControllerSpellsCantBeCounteredByColorsEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantControllerSpellsCantBeCounteredByColorsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (GrantControllerSpellsCantBeCounteredByColorsEffect) effect;
        UUID controllerId = entry.getControllerId();
        gameData.addFloatingEffect(FloatingContinuousEffect.onPlayer(entry.getCard().getName(),
                entry.getSourcePermanentId(), entry.getControllerId(), effect, controllerId,
                Duration.UNTIL_END_OF_TURN));

        String colorNames = e.colors().stream()
                .map(c -> c.name().toLowerCase())
                .sorted()
                .reduce((a, b) -> a + " or " + b)
                .orElse("");
        String logEntry = "Spells " + gameData.playerIdToName.get(controllerId) + " controls can't be countered by " + colorNames + " spells this turn.";
        gameLogService.append(gameData, GameLog.text(logEntry));
    }
}
