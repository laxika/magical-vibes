package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantPlayerStaticEffectsUntilEndOfTurnEffect;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class GrantPlayerStaticEffectsUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantPlayerStaticEffectsUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (GrantPlayerStaticEffectsUntilEndOfTurnEffect) effect;
        gameData.playerStaticEffectsUntilEndOfTurn
                .computeIfAbsent(entry.getControllerId(), ignored -> new ArrayList<>())
                .addAll(e.effects());
    }
}
