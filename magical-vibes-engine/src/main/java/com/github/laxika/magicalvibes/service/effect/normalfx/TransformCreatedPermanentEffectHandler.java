package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TransformCreatedPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a transform instruction for the permanent just created by the same resolution. */
@Component
@RequiredArgsConstructor
public class TransformCreatedPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AnimationSupport animationSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TransformCreatedPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getCreatedPermanentIds().isEmpty()) {
            return;
        }

        Permanent createdPermanent = gameQueryService.findPermanentById(
                gameData, entry.getCreatedPermanentIds().getLast());
        if (createdPermanent == null || gameQueryService.isTransformPrevented(gameData, createdPermanent)) {
            return;
        }

        if (!createdPermanent.isTransformed()) {
            animationSupport.transformToBackFace(gameData, createdPermanent);
        } else {
            animationSupport.transformToFrontFace(gameData, createdPermanent);
        }
    }
}
