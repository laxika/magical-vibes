package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReturnedPermanentsEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Puts counters on the permanents an earlier effect of this resolution returned to the battlefield. */
@Component
@RequiredArgsConstructor
public class PutCounterOnReturnedPermanentsEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCounterOnReturnedPermanentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var counterEffect = (PutCounterOnReturnedPermanentsEffect) effect;
        List<UUID> returnedIds = List.copyOf(entry.getReturnedPermanentIds());
        for (UUID permanentId : returnedIds) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
            if (permanent != null) {
                permanentCounterSupport.placeCounterOnPermanent(gameData, entry, permanent,
                        counterEffect.counterType(), counterEffect.count());
            }
        }
    }
}
