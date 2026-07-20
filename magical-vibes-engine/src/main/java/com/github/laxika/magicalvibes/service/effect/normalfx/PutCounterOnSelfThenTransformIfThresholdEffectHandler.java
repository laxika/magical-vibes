package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnSelfThenTransformIfThresholdEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersFromSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PutCounterOnSelfThenTransformIfThresholdEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCounterOnSelfThenTransformIfThresholdEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutCounterOnSelfThenTransformIfThresholdEffect) effect;
        UUID selfId = entry.getSourcePermanentId() != null ? entry.getSourcePermanentId() : entry.getTargetId();
        Permanent self = gameQueryService.findPermanentById(gameData, selfId);
        if (self == null) {
            return;
        }

        if (gameQueryService.cantHaveCounters(gameData, self)) {
            return;
        }

        // Put the counter
        String counterName = switch (e.counterType()) {
            case CHARGE -> { self.setCounterCount(CounterType.CHARGE, self.getCounterCount(CounterType.CHARGE) + 1); yield "charge"; }
            case HATCHLING -> { self.setCounterCount(CounterType.HATCHLING, self.getCounterCount(CounterType.HATCHLING) + 1); yield "hatchling"; }
            case LANDMARK -> { self.setCounterCount(CounterType.LANDMARK, self.getCounterCount(CounterType.LANDMARK) + 1); yield "landmark"; }
            case SLIME -> { self.setCounterCount(CounterType.SLIME, self.getCounterCount(CounterType.SLIME) + 1); yield "slime"; }
            case STUDY -> { self.setCounterCount(CounterType.STUDY, self.getCounterCount(CounterType.STUDY) + 1); yield "study"; }
            case WISH -> { self.setCounterCount(CounterType.WISH, self.getCounterCount(CounterType.WISH) + 1); yield "wish"; }
            case PLUS_ONE_PLUS_ONE -> { self.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, self.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) + 1); yield "+1/+1"; }
            case MINUS_ONE_MINUS_ONE -> {
                if (gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, self)) { yield null; }
                if (gameQueryService.reduceMinusOneMinusOneCounters(gameData, self, 1) <= 0) { yield null; }
                self.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, self.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE) + 1);
                yield "-1/-1";
            }
            default -> throw new IllegalStateException("Unsupported counter type: " + e.counterType());
        };
        if (counterName == null) return;

        String logEntry = self.getCard().getName() + " gets a " + counterName + " counter.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.builder().card(self.getCard()).text(" gets a " + counterName + " counter.").build());
        log.info("Game {} - {} gets a {} counter", gameData.id, self.getCard().getName(), counterName);

        if (e.counterType() == CounterType.MINUS_ONE_MINUS_ONE) {
            permanentCounterSupport.fireMinusOneMinusOneCounterPutOnCreatureTriggers(gameData, self, 1);
        }

        // Check threshold and transform if met
        int currentCount = switch (e.counterType()) {
            case CHARGE -> self.getCounterCount(CounterType.CHARGE);
            case HATCHLING -> self.getCounterCount(CounterType.HATCHLING);
            case LANDMARK -> self.getCounterCount(CounterType.LANDMARK);
            case SLIME -> self.getCounterCount(CounterType.SLIME);
            case STUDY -> self.getCounterCount(CounterType.STUDY);
            case WISH -> self.getCounterCount(CounterType.WISH);
            case PLUS_ONE_PLUS_ONE -> self.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE);
            case MINUS_ONE_MINUS_ONE -> self.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE);
            default -> 0;
        };

        if (currentCount >= e.threshold()) {
            if (e.optional()) {
                // "you may remove those counters and transform it" — put may ability on the stack
                gameData.queueMayAbility(
                        entry.getCard(),
                        entry.getControllerId(),
                        new MayEffect(
                                SequenceEffect.of(
                                        new RemoveAllCountersFromSelfEffect(e.counterType()),
                                        new TransformSelfEffect()
                                ),
                                "Remove counters and transform?"
                        ),
                        null,
                        selfId
                );
            } else {
                permanentCounterSupport.removeCountersAndTransform(gameData, self, e.counterType(), counterName);
                // Append on-transform effects to the resolving entry so they are picked up
                // by the EffectResolutionService's for-loop (e.g. Treasure Map creates tokens)
                if (!e.onTransformEffects().isEmpty()) {
                    entry.getEffectsToResolve().addAll(e.onTransformEffects());
                }
            }
        }
    }
}
