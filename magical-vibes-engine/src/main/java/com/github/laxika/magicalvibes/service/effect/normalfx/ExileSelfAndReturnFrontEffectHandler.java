package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnFrontEffect;
import com.github.laxika.magicalvibes.service.battlefield.ExileAndReturnTransformedService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExileSelfAndReturnFrontEffectHandler implements NormalEffectHandlerBean {

    private final ExileAndReturnTransformedService exileAndReturnTransformedService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileSelfAndReturnFrontEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getSourcePermanentId() != null) {
            exileAndReturnTransformedService.exileAndReturnFront(gameData, entry.getSourcePermanentId());
        }
    }
}
