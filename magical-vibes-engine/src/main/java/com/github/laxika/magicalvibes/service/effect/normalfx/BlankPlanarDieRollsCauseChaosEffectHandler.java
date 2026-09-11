package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BlankPlanarDieRollsCauseChaosEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Chaotic Aether's encounter effect for its face-up planar object. */
@Component
@RequiredArgsConstructor
public class BlankPlanarDieRollsCauseChaosEffectHandler implements NormalEffectHandlerBean {
    private final PlanechaseService planechase;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BlankPlanarDieRollsCauseChaosEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getSourcePlanarObject() != null) {
            planechase.enableBlankPlanarDieRollChaos(gameData, entry.getSourcePlanarObject().getId());
        }
    }
}
