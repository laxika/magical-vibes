package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.AdjustTimeCountersOnTargetEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AdjustTimeCountersOnTargetEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final RemoveTimeCounterFromExiledCardEffectHandler removeTimeCounterHandler;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AdjustTimeCountersOnTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        AdjustTimeCountersOnTargetEffect adjustment = (AdjustTimeCountersOnTargetEffect) effect;
        List<UUID> targetIds = entry.targetsForEffect(effect);
        UUID targetId = targetIds.isEmpty() ? entry.getTargetId() : targetIds.getFirst();
        if (targetId == null) {
            return;
        }
        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int amount = amountEvaluationService.evaluate(gameData, adjustment.amount(),
                AmountContext.forStackEntry(entry, source));
        if (amount <= 0) {
            return;
        }

        if (entry.getTargetZone() == Zone.EXILE) {
            if (gameData.findExiledCard(targetId) == null
                    || !gameData.exiledCardTimeCounters.containsKey(targetId)) {
                return;
            }
            if (adjustment.add()) {
                gameData.exiledCardTimeCounters.merge(targetId, amount, Integer::sum);
            } else {
                for (int i = 0; i < amount; i++) {
                    removeTimeCounterHandler.removeTimeCounter(gameData, targetId);
                }
            }
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) {
            return;
        }
        if (adjustment.add()) {
            permanentCounterSupport.placeCounterOnPermanent(gameData, entry, target,
                    com.github.laxika.magicalvibes.model.CounterType.TIME, amount);
        } else {
            permanentCounterSupport.removeCountersFromPermanent(gameData, target,
                    com.github.laxika.magicalvibes.model.CounterType.TIME, amount);
        }
    }
}
