package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;

@FunctionalInterface
public interface EffectHandler {
    void resolve(GameData gameData, StackEntry entry, CardEffect effect);
}
