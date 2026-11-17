package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.DeclarationReplacementEffect;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CamouflageEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import org.springframework.stereotype.Component;

/** Registers Camouflage's combat-scoped blocker-declaration replacement. */
@Component
public class CamouflageEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CamouflageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        gameData.addFloatingEffect(FloatingContinuousEffect.onPlayer(entry.getCard().getName(), null,
                entry.getControllerId(), new DeclarationReplacementEffect(
                        DeclarationReplacementEffect.Kind.BLOCKERS_IN_PILES, entry.getCard()), null,
                Duration.UNTIL_END_OF_TURN));
    }
}
