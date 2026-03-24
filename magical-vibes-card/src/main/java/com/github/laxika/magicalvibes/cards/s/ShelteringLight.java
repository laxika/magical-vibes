package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "XLN", collectorNumber = "35")
public class ShelteringLight extends Card {

    public ShelteringLight() {
        addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.TARGET));
        addEffect(EffectSlot.SPELL, new ScryEffect(1));
    }
}
