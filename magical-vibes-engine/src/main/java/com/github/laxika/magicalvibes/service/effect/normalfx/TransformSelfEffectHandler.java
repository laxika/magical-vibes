package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransformSelfEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final SubjectResolutionService subjectResolutionService;
    private final AnimationSupport animationSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TransformSelfEffect.class;
    }

    /**
     * Transforms each permanent the subject names and records a {@link StepKind#TRANSFORM} step ("transform it. If you
     * do"): those that transformed, and their controllers; nothing for one that is gone, a source that transformed since
     * the ability was put on the stack (CR 701.27g), one that can't transform or has no other face.
     */
    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        entry.recordStep(StepKind.TRANSFORM, StepOutcome.NONE);
        PermanentSubject subject = ((TransformSelfEffect) effect).subject();
        Permanent snapshot = entry.getSourcePermanentSnapshot();
        List<UUID> transformedIds = new ArrayList<>();
        List<UUID> controllers = new ArrayList<>();
        for (Permanent self : subjectResolutionService.permanents(gameData, entry, effect, subject)) {
            if (subject instanceof PermanentSubject.Source && snapshot != null
                    && snapshot.getId().equals(self.getId())
                    && snapshot.getTransformationSequence() != self.getTransformationSequence()) {
                continue;
            }
            if (gameQueryService.isTransformPrevented(gameData, self)) {
                log.info("Game {} - {} can't transform (transform prevented)", gameData.id, self.getCard().getName());
                continue;
            }
            boolean transformed = self.isTransformed()
                    ? animationSupport.transformToFrontFace(gameData, self)
                    : animationSupport.transformToBackFace(gameData, self);
            if (transformed) {
                transformedIds.add(self.getId());
                controllers.add(gameQueryService.findPermanentController(gameData, self.getId()));
            }
        }
        if (!transformedIds.isEmpty()) {
            entry.recordStep(StepKind.TRANSFORM, StepOutcome.ofObjects(transformedIds, controllers));
        }
    }
}
