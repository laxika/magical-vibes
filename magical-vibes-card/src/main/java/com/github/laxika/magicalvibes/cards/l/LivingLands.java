package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllLandsAreCreaturesEffect;

@CardRegistration(set = "6ED", collectorNumber = "238")
@CardRegistration(set = "5ED", collectorNumber = "312")
@CardRegistration(set = "4ED", collectorNumber = "260")
public class LivingLands extends Card {

    public LivingLands() {
        // All Forests are 1/1 creatures that are still lands.
        addEffect(EffectSlot.STATIC, new AllLandsAreCreaturesEffect(1, 1, CardSubtype.FOREST));
    }
}
