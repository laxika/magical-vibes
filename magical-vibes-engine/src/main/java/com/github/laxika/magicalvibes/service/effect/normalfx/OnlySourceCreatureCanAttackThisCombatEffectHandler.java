package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.OnlySourceCreatureCanAttackThisCombatEffect;
import org.springframework.stereotype.Component;

/** Resolves the source-creature-only restriction for an additional combat phase. */
@Component
public class OnlySourceCreatureCanAttackThisCombatEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return OnlySourceCreatureCanAttackThisCombatEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        gameData.onlyPermanentCanAttackThisCombatId = entry.getSourcePermanentId();
    }
}
