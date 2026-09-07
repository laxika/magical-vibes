package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

@CardRegistration(set = "IKO", collectorNumber = "37")
public class Vulpikeet extends Card {

    public Vulpikeet() {
        addEffect(EffectSlot.ON_SELF_MUTATES, new PutCountersOnSourceEffect(1, 1, 1));
    }
}
