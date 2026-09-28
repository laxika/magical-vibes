package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CompleteChecklistObjectiveEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/** Resolves a persistent checklist objective and schedules its completion effect. */
@Component
@RequiredArgsConstructor
public class CompleteChecklistObjectiveEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CompleteChecklistObjectiveEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var objectiveEffect = (CompleteChecklistObjectiveEffect) effect;
        Permanent source = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null || !source.getChosenModeLabels().add(objectiveEffect.objective())
                || !source.getChosenModeLabels().containsAll(objectiveEffect.objectives())) {
            return;
        }

        int effectIndex = entry.getEffectsToResolve().indexOf(effect);
        if (effectIndex >= 0) {
            entry.insertEffectsToResolve(effectIndex + 1, List.of(objectiveEffect.completionEffect()));
        }
    }
}
