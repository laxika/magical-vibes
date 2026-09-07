package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnTargetForEachDyingSourcePowerEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves the snapshotted power counter placement of a dying creature. */
@Component
@RequiredArgsConstructor
public class PutCountersOnTargetForEachDyingSourcePowerEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCountersOnTargetForEachDyingSourcePowerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutCountersOnTargetForEachDyingSourcePowerEffect) effect;
        if (e.count() <= 0 || entry.getTargetId() == null) {
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }
        permanentCounterSupport.placeCounterOnPermanent(
                gameData, entry, target, CounterType.PLUS_ONE_PLUS_ONE,
                e.count());
    }
}
