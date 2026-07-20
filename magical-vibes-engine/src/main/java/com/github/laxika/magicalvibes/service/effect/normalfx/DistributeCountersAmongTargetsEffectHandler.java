package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DistributeCountersAmongTargetsEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link DistributeCountersAmongTargetsEffect}: splits {@code total} counters evenly across
 * the effect's chosen target group and places {@code floor(total / targetCount)} counters on each
 * surviving target. Placement routes through {@link PermanentCounterSupport#placeCounterOnPermanent}
 * so counter-type-specific behaviour (-1/-1 prevention/reduction, +1/+1 triggers) is preserved.
 */
@Component
@RequiredArgsConstructor
public class DistributeCountersAmongTargetsEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DistributeCountersAmongTargetsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DistributeCountersAmongTargetsEffect) effect;

        List<UUID> targetIds = entry.targetsForEffect(effect);
        if (targetIds.isEmpty()) {
            return;
        }
        int countPerTarget = e.total() / targetIds.size();
        if (countPerTarget <= 0) {
            return;
        }

        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                continue; // Partially resolves — skip targets that left the battlefield.
            }
            permanentCounterSupport.placeCounterOnPermanent(gameData, entry, target, e.counterType(), countPerTarget);
        }
    }
}
