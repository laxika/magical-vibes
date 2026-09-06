package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllLandsAreCreaturesEffect;

@CardRegistration(set = "LEG", collectorNumber = "193")
public class LivingPlane extends Card {

    public LivingPlane() {
        // All lands are 1/1 creatures that are still lands.
        addEffect(EffectSlot.STATIC, new AllLandsAreCreaturesEffect(1, 1));
    }
}
