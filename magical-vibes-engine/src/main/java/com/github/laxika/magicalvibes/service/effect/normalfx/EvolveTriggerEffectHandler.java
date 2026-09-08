package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EvolveTriggerEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EvolveTriggerEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EvolveTriggerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getSourcePermanentId() == null || entry.getTriggeringPermanentId() == null) {
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null || gameQueryService.cantHaveCounters(gameData, source)) {
            return;
        }

        Permanent entering = gameQueryService.findPermanentById(gameData, entry.getTriggeringPermanentId());
        int enteringPower = entering != null
                ? gameQueryService.getEffectivePower(gameData, entering)
                : valueOrZero(entry.getTriggeringPermanentPowerAtTrigger());
        int enteringToughness = entering != null
                ? gameQueryService.getEffectiveToughness(gameData, entering)
                : valueOrZero(entry.getTriggeringPermanentToughnessAtTrigger());

        if (enteringPower <= gameQueryService.getEffectivePower(gameData, source)
                && enteringToughness <= gameQueryService.getEffectiveToughness(gameData, source)) {
            return;
        }

        EvolveTriggerEffect evolve = (EvolveTriggerEffect) effect;
        int before = source.getCounterCount(evolve.counterType());
        permanentCounterSupport.placeCounterOnPermanent(gameData, entry, source, evolve.counterType(), 1);
        if (source.getCounterCount(evolve.counterType()) > before
                && evolve.counterType() == CounterType.PLUS_ONE_PLUS_ONE) {
            fireEvolvesTriggers(gameData, source);
        }
    }

    /**
     * The keyword-based evolve ability triggers only when its counter is actually placed.
     */
    private void fireEvolvesTriggers(GameData gameData, Permanent source) {
        List<CardEffect> effects = source.getCard().getEffects(EffectSlot.ON_SELF_EVOLVES);
        if (effects.isEmpty()) {
            return;
        }

        UUID controllerId = gameData.findControllerOf(source);
        if (controllerId == null) {
            return;
        }

        gameData.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                source.getCard(),
                controllerId,
                source.getCard().getName() + "'s triggered ability",
                new ArrayList<>(effects),
                null,
                source.getId()
        ));
        gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s evolve trigger triggers."));
    }

    private static int valueOrZero(Integer value) {
        return value != null ? value : 0;
    }
}
