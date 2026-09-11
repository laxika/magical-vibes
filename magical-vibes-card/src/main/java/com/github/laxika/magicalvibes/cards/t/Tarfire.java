package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "LRW", collectorNumber = "194")
@CardRegistration(set = "DD1", collectorNumber = "56")
@CardRegistration(set = "EVG", collectorNumber = "56")
public class Tarfire extends Card {

    public Tarfire() {
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(2));
    }
}
