package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.PlanarDiePlaneswalkToChaosEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Registers Fixed Point in Time's planar-die replacement until its controller's next turn. */
@Component
public class PlanarDiePlaneswalkToChaosEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PlanarDiePlaneswalkToChaosEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(),
                entry.getCard() == null ? null : entry.getCard().getName(),
                null,
                entry.getControllerId(),
                effect,
                null,
                null,
                null,
                EffectDuration.UNTIL_YOUR_NEXT_TURN,
                0L));
    }
}
