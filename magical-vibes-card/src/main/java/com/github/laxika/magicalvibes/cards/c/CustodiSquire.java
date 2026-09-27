package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.WillOfTheCouncilEffect;

@CardRegistration(set = "CMM", collectorNumber = "19")
public class CustodiSquire extends Card {

    public CustodiSquire() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new WillOfTheCouncilEffect(true));
    }
}
