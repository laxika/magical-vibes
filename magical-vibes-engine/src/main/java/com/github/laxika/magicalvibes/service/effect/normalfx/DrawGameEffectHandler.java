package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawGameEffect;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves an effect that declares the game a draw. */
@Component
@RequiredArgsConstructor
public class DrawGameEffectHandler implements NormalEffectHandlerBean {

    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DrawGameEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        gameOutcomeService.declareDraw(gameData);
    }
}
