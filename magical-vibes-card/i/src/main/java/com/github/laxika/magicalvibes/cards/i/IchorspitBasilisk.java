package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ToxicEffect;

@CardRegistration(set = "ONE", collectorNumber = "170")
public class IchorspitBasilisk extends Card {

    public IchorspitBasilisk() {
        addStaticEffect(new ToxicEffect(1));
    }
}
