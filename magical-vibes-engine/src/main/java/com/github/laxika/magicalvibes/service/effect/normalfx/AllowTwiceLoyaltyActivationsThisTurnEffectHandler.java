package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AllowTwiceLoyaltyActivationsThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import org.springframework.stereotype.Component;

@Component
public class AllowTwiceLoyaltyActivationsThisTurnEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AllowTwiceLoyaltyActivationsThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        gameData.playersAllowedTwiceLoyaltyActivationsThisTurn.add(entry.getControllerId());
    }
}
