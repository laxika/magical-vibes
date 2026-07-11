package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BecomePreparedEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link BecomePreparedEffect}: the source permanent becomes prepared.
 */
@Component
@RequiredArgsConstructor
public class BecomePreparedEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PreparedSupport preparedSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BecomePreparedEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getSourcePermanentId() == null) {
            return;
        }
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        preparedSupport.preparePermanent(gameData, source, entry.getControllerId());
    }
}
