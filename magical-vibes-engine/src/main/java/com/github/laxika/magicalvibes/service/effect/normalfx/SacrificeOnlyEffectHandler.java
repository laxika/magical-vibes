package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeOnlyEffect;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SacrificeOnlyEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeOnlyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<CardEffect> effects = entry.getEffectsToResolve();
        for (int i = 0; i < effects.size(); i++) {
            if (effects.get(i) == effect) {
                entry.insertEffectsToResolve(i + 1, List.of(((SacrificeOnlyEffect) effect).wrapped()));
                return;
            }
        }
    }
}
