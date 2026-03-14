package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToToughnessEffect;

@CardRegistration(set = "ISD", collectorNumber = "85")
public class AbattoirGhoul extends Card {

    public AbattoirGhoul() {
        addEffect(EffectSlot.ON_DAMAGED_CREATURE_DIES, new GainLifeEqualToToughnessEffect());
    }
}
