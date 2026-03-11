package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "M11", collectorNumber = "169")
public class DryadsFavor extends Card {

    public DryadsFavor() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.FORESTWALK, GrantScope.ENCHANTED_CREATURE));
    }
}
