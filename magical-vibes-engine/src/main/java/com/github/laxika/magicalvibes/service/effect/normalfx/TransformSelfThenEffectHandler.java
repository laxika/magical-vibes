package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfThenEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransformSelfThenEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AnimationSupport animationSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TransformSelfThenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent self = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (self == null || gameQueryService.isTransformPrevented(gameData, self)) {
            return;
        }

        TransformSelfThenEffect transform = (TransformSelfThenEffect) effect;
        boolean transformed = self.isTransformed()
                ? animationSupport.transformToFrontFace(gameData, self)
                : animationSupport.transformToBackFace(gameData, self);
        if (transformed) {
            entry.insertEffectsToResolve(entry.getEffectsToResolve().size(), transform.effectsOnTransform());
        }
    }
}
