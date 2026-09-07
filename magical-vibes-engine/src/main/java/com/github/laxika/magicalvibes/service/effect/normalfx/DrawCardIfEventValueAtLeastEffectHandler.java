package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardIfEventValueAtLeastEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DrawCardIfEventValueAtLeastEffectHandler implements NormalEffectHandlerBean {

    private final DrawCardEffectHandler drawCardEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DrawCardIfEventValueAtLeastEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        DrawCardIfEventValueAtLeastEffect conditional = (DrawCardIfEventValueAtLeastEffect) effect;
        if (entry.getEventValue() >= conditional.threshold()) {
            drawCardEffectHandler.resolve(gameData, entry, new DrawCardEffect());
        }
    }
}
