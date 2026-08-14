package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToCardTypeEffect;

@CardRegistration(set = "5DN", collectorNumber = "163")
public class VedalkenOrrery extends Card {

    public VedalkenOrrery() {
        addEffect(EffectSlot.STATIC, new GrantFlashToCardTypeEffect(null));
    }
}
