package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfUnlessEscapedEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a source sacrifice that is skipped when the source was cast using escape. */
@Component
@RequiredArgsConstructor
public class SacrificeSelfUnlessEscapedEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final SacrificeSelfEffectHandler sacrificeSelfEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeSelfUnlessEscapedEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getSourcePermanentId() == null) {
            return;
        }
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null || source.isEscaped()) {
            return;
        }
        sacrificeSelfEffectHandler.resolve(gameData, entry, new SacrificeSelfEffect());
    }
}
