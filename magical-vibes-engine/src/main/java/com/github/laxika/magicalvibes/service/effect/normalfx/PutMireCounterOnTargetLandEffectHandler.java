package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutMireCounterOnTargetLandEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PutMireCounterOnTargetLandEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutMireCounterOnTargetLandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID tombPermanentId = entry.getSourcePermanentId();
        List<UUID> targetIds = entry.targetsForEffect(effect);
        if (targetIds.isEmpty() && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }
        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null || !gameQueryService.isLand(gameData, target)
                    || gameQueryService.effectiveBasicLandTypes(gameData, target)
                    .contains(CardSubtype.SWAMP)
                    || gameQueryService.cantHaveCounters(gameData, target)) {
                continue;
            }
            int placed = permanentCounterSupport.placeCounterOnPermanent(
                    gameData, entry, target, CounterType.MIRE, 1);
            if (placed > 0 && tombPermanentId != null) {
                gameData.cyclopeanTombMireTargets
                        .computeIfAbsent(tombPermanentId, ignored -> java.util.concurrent.ConcurrentHashMap.newKeySet())
                        .add(targetId);
            }
        }
    }
}
