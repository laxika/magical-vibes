package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "TMP", collectorNumber = "86")
public class ShadowRift extends Card {

    public ShadowRift() {
        addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.SHADOW, GrantScope.TARGET));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
    }
}
