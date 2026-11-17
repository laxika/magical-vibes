package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectAllCreatureDamageToControllerEffect;
import org.springframework.stereotype.Component;

@Component
public class RedirectAllCreatureDamageToControllerEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RedirectAllCreatureDamageToControllerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getControllerId() != null) {
            gameData.addFloatingEffect(FloatingContinuousEffect.onPlayer(entry.getCard().getName(),
                    entry.getSourcePermanentId(), entry.getControllerId(),
                    (RedirectAllCreatureDamageToControllerEffect) effect, entry.getControllerId(),
                    Duration.UNTIL_END_OF_TURN));
        }
    }
}
