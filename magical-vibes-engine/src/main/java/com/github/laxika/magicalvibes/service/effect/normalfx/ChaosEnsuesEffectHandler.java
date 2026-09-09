package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChaosEnsuesEffect;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChaosEnsuesEffectHandler implements NormalEffectHandlerBean {
    private final PlanechaseService planechase;

    @Override
    public Class<? extends CardEffect> handledEffect() { return ChaosEnsuesEffect.class; }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        planechase.chaos(gameData);
    }
}
