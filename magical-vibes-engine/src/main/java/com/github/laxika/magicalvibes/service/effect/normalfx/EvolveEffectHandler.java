package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EvolveEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.trigger.CounterAbilityReader;
import com.github.laxika.magicalvibes.service.trigger.CounterWatch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EvolveEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EvolveEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null || gameQueryService.cantHaveCounters(gameData, source)) {
            return;
        }
        int before = source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE);
        permanentCounterSupport.placeCounterOnPermanent(gameData, entry, source, CounterType.PLUS_ONE_PLUS_ONE, 1);
        if (source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) > before) {
            fireEvolvesTriggers(gameData, source);
        }
    }

    /** "Whenever this creature evolves": a +1/+1 counter was put on it by its evolve ability (CR 702.100b). */
    private void fireEvolvesTriggers(GameData gameData, Permanent source) {
        List<CardEffect> effects = CounterAbilityReader.printedEffects(source.getCard(),
                CounterWatch.ON_SELF_EVOLVES);
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
}
