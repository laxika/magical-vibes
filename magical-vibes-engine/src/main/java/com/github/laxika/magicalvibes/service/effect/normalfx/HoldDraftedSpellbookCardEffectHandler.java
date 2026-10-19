package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.HoldDraftedSpellbookCardEffect;
import org.springframework.stereotype.Component;

@Component
public class HoldDraftedSpellbookCardEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return HoldDraftedSpellbookCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var hold = (HoldDraftedSpellbookCardEffect) effect;
        if (hold.card() != null) {
            gameData.heldSpellbookCards.add(hold.card());
        }
    }
}
