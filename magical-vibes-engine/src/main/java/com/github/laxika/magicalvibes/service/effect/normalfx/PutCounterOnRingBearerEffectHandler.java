package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnRingBearerEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves counter placement on the controller's Ring-bearer. */
@Component
@RequiredArgsConstructor
public class PutCounterOnRingBearerEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCounterOnRingBearerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        PutCounterOnRingBearerEffect counterEffect = (PutCounterOnRingBearerEffect) effect;
        UUID controllerId = entry.getControllerId();
        UUID ringBearerId = controllerId == null ? null : gameData.ringBearerIds.get(controllerId);
        Permanent ringBearer = ringBearerId == null
                ? null : gameQueryService.findPermanentById(gameData, ringBearerId);
        if (ringBearer == null || !controllerId.equals(
                gameQueryService.findPermanentController(gameData, ringBearerId))) {
            return;
        }

        permanentCounterSupport.placeCounterOnPermanent(
                gameData, entry, ringBearer, counterEffect.counterType(), counterEffect.amount());
    }
}
