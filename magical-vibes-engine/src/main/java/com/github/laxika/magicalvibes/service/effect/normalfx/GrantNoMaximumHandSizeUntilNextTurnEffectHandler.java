package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantNoMaximumHandSizeUntilNextTurnEffect;
import org.springframework.stereotype.Component;

/**
 * Grants the resolving controller no maximum hand size until their next turn begins.
 */
@Component
public class GrantNoMaximumHandSizeUntilNextTurnEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantNoMaximumHandSizeUntilNextTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        gameData.playersWithNoMaximumHandSizeUntilNextTurn.add(entry.getControllerId());
    }
}
