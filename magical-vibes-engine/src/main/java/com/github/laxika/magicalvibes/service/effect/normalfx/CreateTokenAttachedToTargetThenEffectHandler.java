package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenAttachedToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenAttachedToTargetThenEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreateTokenAttachedToTargetThenEffectHandler implements NormalEffectHandlerBean {

    private final CreateTokenAttachedToTargetEffectHandler createTokenAttachedToTargetEffectHandler;
    private final CreateTokenThenEffectHandler createTokenThenEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenAttachedToTargetThenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CreateTokenAttachedToTargetThenEffect createThen = (CreateTokenAttachedToTargetThenEffect) effect;
        UUID targetId = entry.getTargetId();
        int createdBefore = entry.getCreatedPermanentIds().size();

        createTokenAttachedToTargetEffectHandler.resolve(gameData, entry,
                new CreateTokenAttachedToTargetEffect(
                        createThen.tokenEffect(), createThen.targetControllerRelation()));
        if (gameData.resolvingMayEffectFromStack || !gameData.pendingMayAbilities.isEmpty()) {
            return;
        }
        if (targetId == null || entry.getCreatedPermanentIds().size() == createdBefore) {
            return;
        }

        createTokenThenEffectHandler.queueTargetedReflexiveAbility(
                gameData, entry, createThen.thenEffect(), targetId, true);
    }
}
