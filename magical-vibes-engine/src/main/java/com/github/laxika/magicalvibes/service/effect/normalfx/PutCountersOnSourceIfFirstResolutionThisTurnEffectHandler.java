package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceIfFirstResolutionThisTurnEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class PutCountersOnSourceIfFirstResolutionThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCountersOnSourceIfFirstResolutionThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null) {
            return;
        }

        var firstResolutions = gameData.firstResolutionTriggerKeysThisTurn
                .computeIfAbsent(sourcePermanentId, ignored -> ConcurrentHashMap.newKeySet());
        var firstResolution = (PutCountersOnSourceIfFirstResolutionThisTurnEffect) effect;
        if (!firstResolutions.add(firstResolution.abilityKey())) {
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source != null) {
            permanentCounterSupport.placeCounterOnPermanent(
                    gameData, entry, source, firstResolution.counterType(), firstResolution.count());
        }
    }
}
