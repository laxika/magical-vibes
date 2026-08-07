package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantControllerKeywordEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "EVE", collectorNumber = "14")
public class SpiritOfTheHearth extends Card {

    public SpiritOfTheHearth() {
        addEffect(EffectSlot.STATIC, new GrantControllerKeywordEffect(Keyword.HEXPROOF));
    }
}
