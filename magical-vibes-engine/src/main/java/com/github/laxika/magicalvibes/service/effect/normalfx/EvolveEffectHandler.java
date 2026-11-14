package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.rules.CounterChangeEvent;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EvolveEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EvolveEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;

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
        UUID controllerId = gameData.findControllerOf(source);
        if (controllerId == null) {
            return;
        }
        // Each ability, whole (CR 603.3).
        gameData.rulesEvents.occur(CounterChangeEvent.of(CounterChangeEvent.Kind.EVOLVED, source, controllerId, null,
                1));
    }
}
