package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringCreatureUntilSourceLeavesAndReturnOthersEffect;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExileTriggeringCreatureUntilSourceLeavesAndReturnOthersEffectHandler
        implements NormalEffectHandlerBean {

    private final ExileTriggeringCreatureUntilSourceLeavesEffectHandler exileHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTriggeringCreatureUntilSourceLeavesAndReturnOthersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        exileHandler.resolveAndReturnOthers(gameData, entry);
    }
}
