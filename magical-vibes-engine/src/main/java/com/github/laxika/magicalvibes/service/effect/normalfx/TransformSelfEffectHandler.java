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
import java.util.List;
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
     * Transforms the source and records a {@link StepKind#TRANSFORM} step ("transform it. If you do"): the source,
     * and its controller, when it transformed; nothing when it is gone, transformed since the ability was put on the
     * stack (CR 701.27g), can't transform or has no other face.
     */
    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        entry.recordStep(StepKind.TRANSFORM, StepOutcome.NONE);
        PermanentSubject subject = ((TransformSelfEffect) effect).subject();
        Permanent self = subjectResolutionService.permanent(gameData, entry, effect, subject);
        if (self == null) {
            return;
        }
        Permanent snapshot = entry.getSourcePermanentSnapshot();
        if (subject instanceof PermanentSubject.Source && snapshot != null && snapshot.getId().equals(self.getId())
                && snapshot.getTransformationSequence() != self.getTransformationSequence()) {
            return;
        }

        if (gameQueryService.isTransformPrevented(gameData, self)) {
            log.info("Game {} - {} can't transform (transform prevented)", gameData.id, self.getCard().getName());
            return;
        }

        boolean transformed = self.isTransformed()
                ? animationSupport.transformToFrontFace(gameData, self)
                : animationSupport.transformToBackFace(gameData, self);
        if (transformed) {
            entry.recordStep(StepKind.TRANSFORM, StepOutcome.ofObjects(List.of(self.getId()),
                    List.of(gameQueryService.findPermanentController(gameData, self.getId()))));
        }
    }
}
