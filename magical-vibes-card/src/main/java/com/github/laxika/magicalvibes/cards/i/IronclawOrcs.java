package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockCreaturesWithPowerAtLeastEffect;

@CardRegistration(set = "5ED", collectorNumber = "245")
@CardRegistration(set = "4ED", collectorNumber = "206")
public class IronclawOrcs extends Card {

    public IronclawOrcs() {
        addEffect(EffectSlot.STATIC, new CantBlockCreaturesWithPowerAtLeastEffect(2));
    }
}
