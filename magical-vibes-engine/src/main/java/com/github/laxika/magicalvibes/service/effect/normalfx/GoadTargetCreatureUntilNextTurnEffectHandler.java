package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GoadTargetCreatureUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link GoadTargetCreatureUntilNextTurnEffect}: one floating combat requirement for each creature its target or
 * subject names, for the effect's duration, the source permanent kept for a "while this remains" duration; records
 * {@link StepKind#GOAD}.
 */
@Component
@RequiredArgsConstructor
public class GoadTargetCreatureUntilNextTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GoadTargetCreatureUntilNextTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var goad = (GoadTargetCreatureUntilNextTurnEffect) effect;
        List<UUID> targetIds;
        PermanentSubject goaded = goad.goaded();
        if (goaded != null) {
            targetIds = subjectResolutionService.permanentIds(gameData, entry, effect, goaded);
        } else {
            targetIds = entry.targetsForEffect(effect);
            if (targetIds.isEmpty() && entry.getTargetId() != null) {
                targetIds = List.of(entry.getTargetId());
            }
        }

        entry.recordStep(StepKind.GOAD, StepOutcome.NONE);
        boolean whileSource = goad.duration().asLongAs() != null;
        if (whileSource && entry.getSourcePermanentId() == null) {
            return;
        }
        List<UUID> goadedIds = new ArrayList<>();
        List<UUID> controllers = new ArrayList<>();
        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null || !gameQueryService.isCreature(gameData, target)) {
                continue;
            }
            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(), FaceDownVisibility.sourceName(gameData, entry),
                    whileSource ? entry.getSourcePermanentId() : null, entry.getControllerId(),
                    effect, targetId, null, null, goad.duration(), 0));
            goadedIds.add(targetId);
            UUID controllerId = gameQueryService.findPermanentController(gameData, targetId);
            if (controllerId != null) {
                controllers.add(controllerId);
            }
        }
        entry.recordStep(StepKind.GOAD, StepOutcome.ofObjects(goadedIds, controllers));
    }
}
