package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenAttachedToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenAttachedToUpToOneTargetEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves the optional-target form of token attachment through the regular attachment handler. */
@Component
@RequiredArgsConstructor
public class CreateTokenAttachedToUpToOneTargetEffectHandler implements NormalEffectHandlerBean {

    private final CreateTokenAttachedToTargetEffectHandler delegate;
    private final CreateTokenEffectHandler createTokenEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenAttachedToUpToOneTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CreateTokenAttachedToUpToOneTargetEffect optionalEffect =
                (CreateTokenAttachedToUpToOneTargetEffect) effect;
        List<UUID> targetIds = entry.targetsForEffect(optionalEffect);
        if (targetIds.isEmpty() && entry.targetsForBoundEffectGroup(optionalEffect) == null
                && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }
        if (targetIds.isEmpty()) {
            createTokenEffectHandler.resolve(gameData, entry, optionalEffect.token());
            return;
        }
        delegate.resolve(gameData, entry, new CreateTokenAttachedToTargetEffect(
                optionalEffect.token(), optionalEffect.targetControllerRelation(), optionalEffect.targetFilter()),
                targetIds);
    }
}
