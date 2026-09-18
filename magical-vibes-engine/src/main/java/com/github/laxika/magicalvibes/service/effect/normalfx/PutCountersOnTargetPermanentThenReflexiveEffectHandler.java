package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnTargetPermanentThenReflexiveEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves target counter placement followed by a successful reflexive ability. */
@Component
@RequiredArgsConstructor
public class PutCountersOnTargetPermanentThenReflexiveEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCountersOnTargetPermanentThenReflexiveEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var counterThen = (PutCountersOnTargetPermanentThenReflexiveEffect) effect;
        Permanent source = entry.getSourcePermanentId() == null
                ? entry.getSourcePermanentSnapshot()
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }

        List<UUID> targetIds = entry.targetsForEffect(effect);
        if (targetIds.isEmpty() && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }

        int placed = 0;
        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null || gameQueryService.cantHaveCounters(gameData, target)
                    || (counterThen.counterType() == CounterType.MINUS_ONE_MINUS_ONE
                    && gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, target))) {
                continue;
            }
            placed += permanentCounterSupport.placeCounterOnPermanent(
                    gameData, entry, target, counterThen.counterType(), counterThen.count());
        }

        if (placed <= 0) {
            return;
        }

        boolean useEventValueAsX = counterThen.reflexiveXValue() != null;
        if (useEventValueAsX) {
            entry.setEventValue(amountEvaluationService.evaluate(
                    gameData,
                    counterThen.reflexiveXValue(),
                    AmountContext.forStackEntry(entry, source)));
        }

        int effectIndex = entry.getEffectsToResolve().indexOf(effect);
        if (effectIndex < 0) {
            throw new IllegalStateException(
                    "PutCountersOnTargetPermanentThenReflexiveEffect is not part of the resolving entry");
        }
        entry.insertEffectsToResolve(effectIndex + 1,
                List.of(new QueueReflexiveAbilityEffect(
                        counterThen.reflexiveEffect(), counterThen.reflexiveOptionalTarget(), useEventValueAsX)));
    }
}
