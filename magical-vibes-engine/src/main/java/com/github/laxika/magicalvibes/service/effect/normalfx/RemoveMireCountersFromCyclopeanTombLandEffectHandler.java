package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.CyclopeanTombMireCleanup;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveMireCountersFromCyclopeanTombLandEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RemoveMireCountersFromCyclopeanTombLandEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveMireCountersFromCyclopeanTombLandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var cleanup = (RemoveMireCountersFromCyclopeanTombLandEffect) effect;
        List<UUID> targetIds = entry.targetsForEffect(effect);
        if (targetIds.isEmpty() && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }
        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null || !gameQueryService.isLand(gameData, target)) {
                continue;
            }
            permanentCounterSupport.removeCounterFromPermanent(
                    gameData, target, CounterType.MIRE, target.getCounterCount(CounterType.MIRE));
            gameData.cyclopeanTombMireTargets.computeIfPresent(cleanup.tombPermanentId(),
                    (ignored, tracked) -> {
                        var remaining = new HashSet<>(tracked);
                        remaining.remove(targetId);
                        return remaining.isEmpty() ? null : remaining;
                    });
            gameData.clearDelayedActions(CyclopeanTombMireCleanup.class,
                    action -> action.tombPermanentId().equals(cleanup.tombPermanentId()));
            // Recreate the recurring action with the selected land removed from its remembered set.
            // The trigger was re-queued by StepTriggerService before the choice was presented.
            var remaining = gameData.cyclopeanTombMireTargets.get(cleanup.tombPermanentId());
            if (remaining != null && !remaining.isEmpty()) {
                gameData.queueDelayedAction(new CyclopeanTombMireCleanup(
                        cleanup.tombPermanentId(), entry.getControllerId(), entry.getCard(),
                        new HashSet<>(remaining)));
            }
        }
    }
}
