package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantControllerKeywordEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "53")
@CardRegistration(set = "ONS", collectorNumber = "57")
public class TrueBeliever extends Card {

    public TrueBeliever() {
        addEffect(EffectSlot.STATIC, new GrantControllerKeywordEffect(Keyword.SHROUD));
    }
}
