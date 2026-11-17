package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MakeNextInstantSorceryUncounterableThisTurnEffect;
import org.springframework.stereotype.Component;
import com.github.laxika.magicalvibes.model.effect.Duration;

@Component
public class MakeNextInstantSorceryUncounterableThisTurnEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MakeNextInstantSorceryUncounterableThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        gameData.addPlayerEffect(entry, effect, entry.getControllerId(), Duration.UNTIL_MATCHING_SPELL_CAST);
    }
}
