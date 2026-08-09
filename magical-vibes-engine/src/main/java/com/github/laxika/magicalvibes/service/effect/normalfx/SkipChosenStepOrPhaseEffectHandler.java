package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SkipChosenStepOrPhaseEffect;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Stores the selected step or phase for the affected player until cleanup. */
@Component
public class SkipChosenStepOrPhaseEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SkipChosenStepOrPhaseEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID playerId = entry.getTargetId() != null ? entry.getTargetId() : entry.getControllerId();
        SkipChosenStepOrPhaseEffect skip = (SkipChosenStepOrPhaseEffect) effect;
        gameData.skippedStepOrPhasesThisTurn
                .computeIfAbsent(playerId, ignored -> ConcurrentHashMap.newKeySet())
                .add(skip.kind());
    }
}
