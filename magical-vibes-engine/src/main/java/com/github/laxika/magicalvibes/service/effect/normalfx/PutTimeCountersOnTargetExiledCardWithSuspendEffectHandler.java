package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutTimeCountersOnTargetExiledCardWithSuspendEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PutTimeCountersOnTargetExiledCardWithSuspendEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutTimeCountersOnTargetExiledCardWithSuspendEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effectToResolve) {
        var effect = (PutTimeCountersOnTargetExiledCardWithSuspendEffect) effectToResolve;
        List<UUID> targetIds = entry.targetsForEffect(effect);
        UUID targetId = targetIds.isEmpty() ? entry.getTargetId() : targetIds.getFirst();
        if (targetId == null) {
            return;
        }

        ExiledCardEntry exiled = gameData.findExiledCard(targetId);
        if (exiled == null || exiled.faceDown()) {
            return;
        }

        gameData.exiledCardTimeCounters.merge(targetId, effect.amount(), Integer::sum);
        gameData.exiledCardsWithNonSuspendTimeCounters.remove(targetId);
        gameLogService.append(gameData,
                GameLog.cardThen(exiled.card(), " gets " + effect.amount()
                        + " time counters and gains suspend."));
    }
}
