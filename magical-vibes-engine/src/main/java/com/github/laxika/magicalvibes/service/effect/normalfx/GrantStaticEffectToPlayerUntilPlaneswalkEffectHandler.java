package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToPlayerUntilPlaneswalkEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class GrantStaticEffectToPlayerUntilPlaneswalkEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantStaticEffectToPlayerUntilPlaneswalkEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var grant = (GrantStaticEffectToPlayerUntilPlaneswalkEffect) effect;
        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(),
                entry.getCard().getName(),
                null,
                entry.getControllerId(),
                grant.staticEffect(),
                null,
                entry.getControllerId(),
                null,
                EffectDuration.UNTIL_PLANESWALK,
                0L));
    }
}
