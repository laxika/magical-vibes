package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterOrSacrificeSelfThenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves counter-removal upkeep abilities with a sacrifice-and-reward fallback. */
@Component
@RequiredArgsConstructor
public class RemoveCounterOrSacrificeSelfThenEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final SacrificeSelfThenEffectHandler sacrificeSelfThenEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveCounterOrSacrificeSelfThenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RemoveCounterOrSacrificeSelfThenEffect) effect;
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }

        if (source.getCounterCount(e.counterType()) > 0) {
            permanentCounterSupport.removeCounterFromPermanent(gameData, source, e.counterType(), 1);
            return;
        }

        sacrificeSelfThenEffectHandler.resolve(gameData, entry, new SacrificeSelfThenEffect(e.thenEffect()));
    }
}
