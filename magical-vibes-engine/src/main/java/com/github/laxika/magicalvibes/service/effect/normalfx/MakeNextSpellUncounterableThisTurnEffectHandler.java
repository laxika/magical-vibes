package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MakeNextSpellUncounterableThisTurnEffect;
import org.springframework.stereotype.Component;

@Component
public class MakeNextSpellUncounterableThisTurnEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MakeNextSpellUncounterableThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        gameData.pendingNextSpellUncounterableThisTurnCount.merge(entry.getControllerId(), 1, Integer::sum);
    }
}
