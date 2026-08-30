package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;

public interface LoyaltyDamageReplacementHandler {

    Class<? extends CardEffect> handledEffect();

    int apply(GameData gameData, Permanent source, Permanent target, int damage);
}
