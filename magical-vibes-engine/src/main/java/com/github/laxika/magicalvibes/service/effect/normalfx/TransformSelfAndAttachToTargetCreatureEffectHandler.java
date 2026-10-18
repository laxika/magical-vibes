package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfAndAttachToTargetCreatureEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Transforms the source permanent and attaches it to the creature targeted by the ability. */
@Component
@RequiredArgsConstructor
public class TransformSelfAndAttachToTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final AnimationSupport animationSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TransformSelfAndAttachToTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetId = entry.getTargetId();
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (targetId == null || sourcePermanentId == null) {
            return;
        }
        animationSupport.completeTransformAndAttach(
                gameData, entry.getControllerId(), sourcePermanentId, targetId);
    }
}
