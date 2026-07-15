package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveChargeCountersFromTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveChargeCountersFromTargetPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveChargeCountersFromTargetPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RemoveChargeCountersFromTargetPermanentEffect) effect;
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        int toRemove = Math.min(e.maxCount(), target.getCounterCount(CounterType.CHARGE));
        if (toRemove > 0) {
            target.setCounterCount(CounterType.CHARGE, target.getCounterCount(CounterType.CHARGE) - toRemove);
            String logEntry = toRemove + " charge counter(s) removed from " + target.getCard().getName() + " (" + target.getCounterCount(CounterType.CHARGE) + " remaining).";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} charge counter(s) removed from {} ({} remaining)", gameData.id, toRemove, target.getCard().getName(), target.getCounterCount(CounterType.CHARGE));
        }
    }
}
