package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllLandsAreCreaturesEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "7ED", collectorNumber = "260")
public class NaturesRevolt extends Card {

    public NaturesRevolt() {
        // All lands are 2/2 creatures that are still lands.
        addEffect(EffectSlot.STATIC, new AllLandsAreCreaturesEffect(2, 2));
    }
}
