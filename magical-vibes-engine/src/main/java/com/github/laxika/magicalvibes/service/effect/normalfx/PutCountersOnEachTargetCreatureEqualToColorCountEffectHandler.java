package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnEachTargetCreatureEqualToColorCountEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves per-target color-count counter placement. */
@Component
@RequiredArgsConstructor
public class PutCountersOnEachTargetCreatureEqualToColorCountEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCountersOnEachTargetCreatureEqualToColorCountEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutCountersOnEachTargetCreatureEqualToColorCountEffect) effect;
        List<UUID> targetIds = entry.targetsForEffect(effect);
        if (targetIds.isEmpty() && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }

        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null || !gameQueryService.isCreature(gameData, target)) {
                continue;
            }

            int count = gameQueryService.getEffectiveColors(gameData, target).size();
            if (count <= 0
                    || gameQueryService.cantHaveCounters(gameData, target)
                    || (e.counterType() == CounterType.MINUS_ONE_MINUS_ONE
                    && gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, target))) {
                continue;
            }

            permanentCounterSupport.placeCounterOnPermanent(gameData, entry, target, e.counterType(), count);
        }
    }
}
