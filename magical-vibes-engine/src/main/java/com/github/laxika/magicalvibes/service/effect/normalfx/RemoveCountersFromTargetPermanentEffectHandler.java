package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCountersFromTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveCountersFromTargetPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveCountersFromTargetPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RemoveCountersFromTargetPermanentEffect) effect;
        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int amount = amountEvaluationService.evaluate(gameData, e.amount(),
                AmountContext.forStackEntry(entry, source));

        List<UUID> targetIds = entry.targetsForEffect(effect);
        if (!targetIds.isEmpty()) {
            for (UUID targetId : targetIds) {
                removeFromTarget(gameData, targetId, e, amount);
            }
            return;
        }

        if (entry.getTargetId() != null) {
            removeFromTarget(gameData, entry.getTargetId(), e, amount);
        }
    }

    private void removeFromTarget(GameData gameData, UUID targetId,
                                  RemoveCountersFromTargetPermanentEffect effect, int amount) {
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null || amount <= 0) {
            return;
        }

        int removed = Math.min(amount, target.getCounterCount(effect.counterType()));
        if (removed <= 0) {
            return;
        }

        target.setCounterCount(effect.counterType(), target.getCounterCount(effect.counterType()) - removed);
        String counterName = permanentCounterSupport.counterTypeName(effect.counterType());
        String prefix = removed == 1
                ? "A " + counterName + " counter removed from "
                : removed + " " + counterName + " counters removed from ";
        gameLogService.append(gameData, GameLog.textCardText(prefix, target.getCard(), "."));
        log.info("Game {} - {} {} counter(s) removed from {}", gameData.id, removed,
                effect.counterType(), target.getCard().getName());
    }
}
