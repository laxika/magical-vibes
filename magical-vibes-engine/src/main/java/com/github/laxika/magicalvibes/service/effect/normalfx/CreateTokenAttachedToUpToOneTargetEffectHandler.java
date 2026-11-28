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
        // Declining the optional target (choosing yourself) leaves only a player id behind. That is
        // zero targets, so there is nothing to attach the token to and it isn't created (CR 303.4g; ruling, 2023-09-01).
        boolean declined = targetIds.stream().allMatch(gameData.orderedPlayerIds::contains);
        if (targetIds.isEmpty() || declined) {
            return;
        }
        delegate.resolveStep(gameData, entry, new CreateTokenAttachedToTargetEffect(
                optionalEffect.token(), optionalEffect.targetControllerRelation(), optionalEffect.targetFilter()),
                targetIds);
    }
}
