package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileDragonApproachAndSearchEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExileDragonApproachAndSearchEffectHandler implements NormalEffectHandlerBean {

    private final ExileDragonApproachAndSearchSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileDragonApproachAndSearchEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        support.begin(gameData, entry);
    }
}
