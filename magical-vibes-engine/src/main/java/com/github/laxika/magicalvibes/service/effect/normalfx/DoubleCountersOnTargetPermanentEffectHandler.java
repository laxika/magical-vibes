package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleCountersOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DoubleCountersOnTargetPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DoubleCountersOnTargetPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> targetIds = entry.getTargetIds();
        if (targetIds.isEmpty()) {
            if (entry.getTargetId() == null) {
                return;
            }
            targetIds = List.of(entry.getTargetId());
        }
        for (UUID targetId : targetIds) {
            doubleCounters(gameData, entry, targetId, (DoubleCountersOnTargetPermanentEffect) effect);
        }
    }

    private void doubleCounters(GameData gameData, StackEntry entry, UUID targetId,
                                DoubleCountersOnTargetPermanentEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) {
            return;
        }

        if (effect.counterType() != null) {
            int current = target.getCounterCount(effect.counterType());
            if (current <= 0) return;
            int before = current;
            permanentCounterSupport.placeCounterOnPermanent(
                    gameData, entry, target, effect.counterType(), current);
            if (target.getCounterCount(effect.counterType()) > before) {
                gameLogService.append(gameData,
                        GameLog.textCardText("Doubled the number of counters on ", target.getCard(), "."));
                log.info("Game {} - doubled {} counters on {}", gameData.id,
                        effect.counterType(), target.getCard().getName());
            }
            return;
        }

        boolean doubledAny = false;
        for (CounterType counterType : CounterType.values()) {
            if (counterType == CounterType.ANY || counterType == CounterType.SILVER) {
                continue;
            }
            int current = target.getCounterCount(counterType);
            if (current > 0) {
                permanentCounterSupport.placeCounterOnPermanent(
                        gameData, entry, target, counterType, current);
                doubledAny = true;
            }
        }

        if (doubledAny) {
            gameLogService.append(gameData, GameLog.textCardText("Doubled the number of each kind of counter on ", target.getCard(), "."));
            log.info("Game {} - doubled counters on {}", gameData.id, target.getCard().getName());
        }
    }
}
