package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTriggeringPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Puts counters on the permanent whose event produced the trigger (Freyalise's Winds' wind counter
 * on the permanent that became tapped). The permanent is read from
 * {@code StackEntry.triggeringPermanentId}; if it has left the battlefield the effect does nothing.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PutCounterOnTriggeringPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCounterOnTriggeringPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutCounterOnTriggeringPermanentEffect) effect;
        if (entry.getTriggeringPermanentId() == null) {
            return;
        }
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTriggeringPermanentId());
        if (target == null || gameQueryService.cantHaveCounters(gameData, target)) {
            return;
        }
        permanentCounterSupport.placeCounterOnPermanent(gameData, entry, target, e.counterType(), e.count());
    }
}
