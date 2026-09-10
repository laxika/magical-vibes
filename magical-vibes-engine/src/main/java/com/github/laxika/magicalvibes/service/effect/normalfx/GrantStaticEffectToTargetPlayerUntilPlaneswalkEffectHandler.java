package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToTargetPlayerUntilPlaneswalkEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class GrantStaticEffectToTargetPlayerUntilPlaneswalkEffectHandler
        implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantStaticEffectToTargetPlayerUntilPlaneswalkEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        GrantStaticEffectToTargetPlayerUntilPlaneswalkEffect grant =
                (GrantStaticEffectToTargetPlayerUntilPlaneswalkEffect) effect;
        List<UUID> targetIds = entry.targetsForEffect(effect);
        if (targetIds.isEmpty() && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }
        if (targetIds.isEmpty() && entry.getTargetIds() != null) {
            targetIds = entry.getTargetIds();
        }

        for (UUID targetId : targetIds) {
            if (!gameData.playerIds.contains(targetId)) {
                continue;
            }
            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(),
                    entry.getCard().getName(),
                    null,
                    entry.getControllerId(),
                    grant.staticEffect(),
                    null,
                    targetId,
                    null,
                    EffectDuration.UNTIL_PLANESWALK,
                    0L));
        }
    }
}
