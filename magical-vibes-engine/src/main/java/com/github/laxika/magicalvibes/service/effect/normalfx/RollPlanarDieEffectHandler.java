package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RollPlanarDieEffect;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RollPlanarDieEffectHandler implements NormalEffectHandlerBean {
    private final PlanechaseService planechase;

    @Override
    public Class<? extends CardEffect> handledEffect() { return RollPlanarDieEffect.class; }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        planechase.roll(gameData, entry.getControllerId());
    }
}
