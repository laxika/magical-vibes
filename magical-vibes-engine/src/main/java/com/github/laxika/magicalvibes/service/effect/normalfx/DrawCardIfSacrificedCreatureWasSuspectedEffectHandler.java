package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardIfSacrificedCreatureWasSuspectedEffect;
import org.springframework.stereotype.Component;

/** Resolves an extra draw based on the sacrificed creature's payment-time designation. */
@Component
public class DrawCardIfSacrificedCreatureWasSuspectedEffectHandler implements NormalEffectHandlerBean {

    private final DrawCardEffectHandler drawCardEffectHandler;

    public DrawCardIfSacrificedCreatureWasSuspectedEffectHandler(DrawCardEffectHandler drawCardEffectHandler) {
        this.drawCardEffectHandler = drawCardEffectHandler;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DrawCardIfSacrificedCreatureWasSuspectedEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getSacrificedPermanentSnapshot() == null
                || !entry.getSacrificedPermanentSnapshot().isSuspected()) {
            return;
        }
        drawCardEffectHandler.resolve(gameData, entry, new DrawCardEffect(1));
    }
}
