package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfEnteringPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a token copy of the permanent that caused an enter-the-battlefield trigger. */
@Component
@RequiredArgsConstructor
public class CreateTokenCopyOfEnteringPermanentEffectHandler implements NormalEffectHandlerBean {

    private final CreateTokenCopyOfTargetPermanentEffectHandler targetPermanentHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopyOfEnteringPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID enteringPermanentId = entry.getTargetId() != null
                ? entry.getTargetId()
                : entry.getTriggeringPermanentId();
        if (enteringPermanentId == null) {
            return;
        }
        targetPermanentHandler.resolveForTarget(
                gameData, entry, new CreateTokenCopyOfTargetPermanentEffect(), enteringPermanentId);
    }
}
