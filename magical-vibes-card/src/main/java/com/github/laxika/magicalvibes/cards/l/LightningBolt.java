package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "M10", collectorNumber = "146")
@CardRegistration(set = "M11", collectorNumber = "149")
@CardRegistration(set = "4ED", collectorNumber = "208")
@CardRegistration(set = "ATH", collectorNumber = "43")
@CardRegistration(set = "BTD", collectorNumber = "41")
@CardRegistration(set = "SUM", collectorNumber = "162")
@CardRegistration(set = "ME1", collectorNumber = "102")
@CardRegistration(set = "3ED", collectorNumber = "162")
@CardRegistration(set = "PD2", collectorNumber = "17")
@CardRegistration(set = "MM2", collectorNumber = "122")
public class LightningBolt extends Card {

    public LightningBolt() {
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(3));
    }
}
