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
        if (source == null) {
            return;
        }
        // Completing an objective that is already checked off still counts as completing it, and the
        // "if all are checked off" check happens on resolution, so a map whose sacrifice could not be
        // performed earlier (for example after changing controllers) completes on the next objective.
        source.getChosenModeLabels().add(objectiveEffect.objective());
        if (!source.getChosenModeLabels().containsAll(objectiveEffect.objectives())) {
            return;
        }

        int effectIndex = entry.getEffectsToResolve().indexOf(effect);
        if (effectIndex >= 0) {
            entry.insertEffectsToResolve(effectIndex + 1, List.of(objectiveEffect.completionEffect()));
        }
    }
}
