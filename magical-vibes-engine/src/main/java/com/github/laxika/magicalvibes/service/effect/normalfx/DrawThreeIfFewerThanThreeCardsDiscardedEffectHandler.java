package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawThreeIfFewerThanThreeCardsDiscardedEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves the conditional draw after a tracked multi-player discard. */
@Component
@RequiredArgsConstructor
public class DrawThreeIfFewerThanThreeCardsDiscardedEffectHandler implements NormalEffectHandlerBean {

    private final DrawCardEffectHandler drawCardEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DrawThreeIfFewerThanThreeCardsDiscardedEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getTriggeringCardIds().size() < 3) {
            drawCardEffectHandler.resolve(gameData, entry, new DrawCardEffect(3));
        }
    }
}
