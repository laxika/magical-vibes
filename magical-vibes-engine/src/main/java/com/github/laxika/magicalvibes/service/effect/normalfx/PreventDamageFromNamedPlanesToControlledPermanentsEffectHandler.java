package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromNamedPlanesToControlledPermanentsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PreventDamageFromNamedPlanesToControlledPermanentsEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PreventDamageFromNamedPlanesToControlledPermanentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PreventDamageFromNamedPlanesToControlledPermanentsEffect) effect;
        UUID controllerId = entry.getControllerId();
        if (controllerId == null || e.planeName() == null || e.planeName().isBlank()) {
            return;
        }

        gameData.addFloatingEffect(FloatingContinuousEffect.onPlayer(entry.getCard().getName(),
                entry.getSourcePermanentId(), controllerId, e, controllerId, Duration.PERMANENT));
        gameLogService.append(gameData, GameLog.text(
                "All damage that planes named " + e.planeName()
                        + " would deal to permanents controlled by "
                        + gameData.playerIdToName.get(controllerId) + " is prevented for the rest of the game."));
    }
}
