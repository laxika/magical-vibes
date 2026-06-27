package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnSelfEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PutCounterOnSelfEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCounterOnSelfEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutCounterOnSelfEffect) effect;
        UUID selfId = entry.getSourcePermanentId() != null ? entry.getSourcePermanentId() : entry.getTargetId();
        Permanent self = gameQueryService.findPermanentById(gameData, selfId);
        if (self == null) {
            return;
        }

        if (gameQueryService.cantHaveCounters(gameData, self)) {
            return;
        }

        String counterName = switch (e.counterType()) {
            case CHARGE -> { self.setCounterCount(CounterType.CHARGE, self.getCounterCount(CounterType.CHARGE) + 1); yield "charge"; }
            case HATCHLING -> { self.setCounterCount(CounterType.HATCHLING, self.getCounterCount(CounterType.HATCHLING) + 1); yield "hatchling"; }
            case SLIME -> { self.setCounterCount(CounterType.SLIME, self.getCounterCount(CounterType.SLIME) + 1); yield "slime"; }
            case STUDY -> { self.setCounterCount(CounterType.STUDY, self.getCounterCount(CounterType.STUDY) + 1); yield "study"; }
            case WISH -> { self.setCounterCount(CounterType.WISH, self.getCounterCount(CounterType.WISH) + 1); yield "wish"; }
            case PLUS_ONE_PLUS_ONE -> { self.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, self.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) + 1); yield "+1/+1"; }
            case MINUS_ONE_MINUS_ONE -> {
                if (gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, self)) { yield null; }
                self.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, self.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE) + 1);
                yield "-1/-1";
            }
            default -> throw new IllegalStateException("Unsupported counter type for PutCounterOnSelfEffect: " + e.counterType());
        };
        if (counterName == null) return;

        String logEntry = self.getCard().getName() + " gets a " + counterName + " counter.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} gets a {} counter", gameData.id, self.getCard().getName(), counterName);
    }
}
