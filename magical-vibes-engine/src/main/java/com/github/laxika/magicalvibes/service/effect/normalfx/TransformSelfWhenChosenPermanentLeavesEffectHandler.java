package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfWhenChosenPermanentLeavesEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransformSelfWhenChosenPermanentLeavesEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AnimationSupport animationSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TransformSelfWhenChosenPermanentLeavesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var transformEffect = (TransformSelfWhenChosenPermanentLeavesEffect) effect;
        UUID sourcePermanentId = entry.getSourcePermanentId();
        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source == null || source.isTransformed()
                || !transformEffect.leavingPermanentId().equals(source.getChosenPermanentId())) {
            return;
        }

        animationSupport.transformToBackFace(gameData, source);
    }
}
