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
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
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
    private final EffectHandlerRegistry effectHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EvolveTriggerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getSourcePermanentId() == null || entry.getTriggeringPermanentId() == null) {
            return;
        }

        EvolveTriggerEffect evolve = (EvolveTriggerEffect) effect;
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        Permanent departedSource = null;
        if (source == null) {
            if (evolve.alsoResolve() == null) {
                return;
            }
            departedSource = gameData.departedPermanentSnapshots.values().stream()
                    .filter(snapshot -> snapshot.getId().equals(entry.getSourcePermanentId()))
                    .findFirst().orElse(null);
            if (departedSource == null) {
                return;
            }
        }
        boolean canPlaceCounter = source != null && !gameQueryService.cantHaveCounters(gameData, source);
        if (!canPlaceCounter && evolve.alsoResolve() == null) {
            return;
        }

        Permanent entering = gameQueryService.findPermanentById(gameData, entry.getTriggeringPermanentId());
        int enteringPower = entering != null
                ? gameQueryService.getEffectivePower(gameData, entering)
                : valueOrZero(entry.getTriggeringPermanentPowerAtTrigger());
        int enteringToughness = entering != null
                ? gameQueryService.getEffectiveToughness(gameData, entering)
                : valueOrZero(entry.getTriggeringPermanentToughnessAtTrigger());

        int sourcePower;
        int sourceToughness;
        if (source != null) {
            sourcePower = gameQueryService.getEffectivePower(gameData, source);
            sourceToughness = gameQueryService.getEffectiveToughness(gameData, source);
        } else {
            sourcePower = departedSource.getLastKnownPower() != null
                    ? departedSource.getLastKnownPower()
                    : gameQueryService.getEffectivePower(gameData, departedSource);
            sourceToughness = departedSource.getLastKnownToughness() != null
                    ? departedSource.getLastKnownToughness()
                    : gameQueryService.getEffectiveToughness(gameData, departedSource);
        }
        if (enteringPower <= sourcePower && enteringToughness <= sourceToughness) {
            return;
        }

        if (canPlaceCounter) {
            int before = source.getCounterCount(evolve.counterType());
            permanentCounterSupport.placeCounterOnPermanent(gameData, entry, source, evolve.counterType(), 1);
            if (source.getCounterCount(evolve.counterType()) > before
                    && evolve.counterType() == CounterType.PLUS_ONE_PLUS_ONE) {
                fireEvolvesTriggers(gameData, source);
            }
        }
        if (evolve.alsoResolve() != null) {
            EffectHandler handler = effectHandlerRegistry.getHandler(evolve.alsoResolve());
            if (handler != null) {
                handler.resolve(gameData, entry, evolve.alsoResolve());
            }
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
