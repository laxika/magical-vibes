package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromGrantingPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link RemoveCounterFromGrantingPermanentEffect}: removes up to the given number of counters
 * from the permanent that granted the activated ability, clamped at zero.
 */
@Component
@RequiredArgsConstructor
public class RemoveCounterFromGrantingPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveCounterFromGrantingPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RemoveCounterFromGrantingPermanentEffect) effect;
        if (e.grantingPermanentId() == null) {
            return;
        }
        Permanent granting = gameQueryService.findPermanentById(gameData, e.grantingPermanentId());
        if (granting == null) {
            return;
        }
        permanentCounterSupport.removeCounterFromPermanent(gameData, granting, e.counterType(), e.count());
    }
}
