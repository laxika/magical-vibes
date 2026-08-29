package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MonstrosityEffect;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonstrosityEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final GameLogService gameLogService;
    private final TriggerCollectionService triggerCollectionService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MonstrosityEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getSourcePermanentId() == null) {
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null || source.isMonstrous()) {
            return;
        }

        var monstrosity = (MonstrosityEffect) effect;
        if (!gameQueryService.cantHaveCounters(gameData, source)) {
            int amount = amountEvaluationService.evaluate(gameData, monstrosity.amount(),
                    AmountContext.forStackEntry(entry, source));
            amount = gameQueryService.doublePlusOnePlusOneCounters(gameData, source, amount);
            if (amount > 0) {
                source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE,
                        source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) + amount);
                permanentCounterSupport.recordPlusOnePlusOneCounterPlacedOnControlledPermanent(
                        gameData, source, amount);
                permanentCounterSupport.firePlusOnePlusOneCountersPutOnAnotherNonHydraCreatureTriggers(
                        gameData, source, amount, entry.getControllerId());
                gameLogService.append(gameData, GameLog.builder().card(source.getCard())
                        .text(" gets " + amount + " +1/+1 counter(s) and becomes monstrous.").build());
                log.info("Game {} - {} becomes monstrous with {} +1/+1 counter(s)", gameData.id,
                        source.getCard().getName(), amount);
            }
        }
        source.setMonstrous(true);

        var controllerId = gameQueryService.findPermanentController(gameData, source.getId());
        if (controllerId != null) {
            triggerCollectionService.checkBecomesMonstrousTriggers(gameData, source, controllerId,
                    entry.getXValue());
        }
    }
}
