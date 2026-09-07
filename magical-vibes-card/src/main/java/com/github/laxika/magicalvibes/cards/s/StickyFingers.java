package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SNC", collectorNumber = "124")
public class StickyFingers extends Card {

    public StickyFingers() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.MENACE, GrantScope.ENCHANTED_CREATURE))
                .addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, CreateTokenEffect.ofTreasureToken(1))
                .addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD, new DrawCardEffect());
    }
}
