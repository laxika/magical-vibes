package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardIfSacrificedPermanentWasCommanderEffect;
import org.springframework.stereotype.Component;

/** Resolves the extra draw based on the sacrificed permanent's last-known designation. */
@Component
public class DrawCardIfSacrificedPermanentWasCommanderEffectHandler implements NormalEffectHandlerBean {

    private final DrawCardEffectHandler drawCardEffectHandler;

    public DrawCardIfSacrificedPermanentWasCommanderEffectHandler(
            DrawCardEffectHandler drawCardEffectHandler) {
        this.drawCardEffectHandler = drawCardEffectHandler;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DrawCardIfSacrificedPermanentWasCommanderEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getSacrificedPermanentSnapshot() == null
                || !entry.getSacrificedPermanentSnapshot().isCommander()) {
            return;
        }
        drawCardEffectHandler.resolve(gameData, entry, new DrawCardEffect(1));
    }
}
