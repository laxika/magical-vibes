package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFreeCastForNextMatchingSpellEffect;
import org.springframework.stereotype.Component;
import com.github.laxika.magicalvibes.model.effect.Duration;

@Component
public class GrantFreeCastForNextMatchingSpellEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantFreeCastForNextMatchingSpellEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var grant = (GrantFreeCastForNextMatchingSpellEffect) effect;
        gameData.addPlayerEffect(entry, grant, entry.getControllerId(), Duration.UNTIL_MATCHING_SPELL_CAST);
    }
}
