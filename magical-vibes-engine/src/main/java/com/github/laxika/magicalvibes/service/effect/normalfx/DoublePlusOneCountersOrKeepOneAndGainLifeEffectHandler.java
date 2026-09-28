package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DoublePlusOneCountersOrKeepOneAndGainLifeEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.MaroGoneNutsSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Lily Bowen's power-dependent upkeep ability. */
@Component
@RequiredArgsConstructor
public class DoublePlusOneCountersOrKeepOneAndGainLifeEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DoublePlusOneCountersOrKeepOneAndGainLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getSourcePermanentId() == null) {
            return;
        }
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }

        var lilyEffect = (DoublePlusOneCountersOrKeepOneAndGainLifeEffect) effect;
        int power = gameQueryService.getEffectivePower(gameData, source);
        int currentCounters = source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE);

        if (power <= lilyEffect.maxPower()) {
            doubleCounters(gameData, entry, source, currentCounters, effect);
            return;
        }

        int removed = Math.max(0, currentCounters - 1);
        if (removed <= 0) {
            return;
        }

        permanentCounterSupport.removeCounterFromPermanent(
                gameData, source, CounterType.PLUS_ONE_PLUS_ONE, removed);
        lifeSupport.applyGainLife(gameData, entry.getControllerId(), removed,
                null, entry.getCard(), entry.getEntryType());
    }

    private void doubleCounters(GameData gameData, StackEntry entry, Permanent source,
                                int currentCounters, CardEffect effect) {
        if (currentCounters <= 0 || gameQueryService.cantHaveCounters(gameData, source)) {
            return;
        }

        int doubledCounters = currentCounters * MaroGoneNutsSupport.apply(gameData, effect, 2);
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, doubledCounters);
        permanentCounterSupport.recordPlusOnePlusOneCounterPlacedOnCreature(
                gameData, source, entry.getControllerId());
        permanentCounterSupport.recordPlusOnePlusOneCounterPlacedOnControlledPermanent(
                gameData, source, doubledCounters - currentCounters, entry.getControllerId());
        permanentCounterSupport.firePlusOnePlusOneCounterTriggers(
                gameData, source, entry.getControllerId());
    }
}
