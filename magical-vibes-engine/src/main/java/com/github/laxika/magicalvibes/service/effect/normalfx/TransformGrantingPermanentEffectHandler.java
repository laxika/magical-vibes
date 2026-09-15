package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TransformGrantingPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransformGrantingPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AnimationSupport animationSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TransformGrantingPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        TransformGrantingPermanentEffect transform = (TransformGrantingPermanentEffect) effect;
        if (transform.grantingPermanentId() == null) {
            return;
        }

        Permanent grantingPermanent = gameQueryService.findPermanentById(
                gameData, transform.grantingPermanentId());
        if (grantingPermanent == null || gameQueryService.isTransformPrevented(gameData, grantingPermanent)) {
            return;
        }

        if (grantingPermanent.isTransformed()) {
            animationSupport.transformToFrontFace(gameData, grantingPermanent);
        } else {
            animationSupport.transformToBackFace(gameData, grantingPermanent);
        }
    }
}
