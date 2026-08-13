package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;

@CardRegistration(set = "M15", collectorNumber = "148")
@CardRegistration(set = "USG", collectorNumber = "199")
public class HeatRay extends Card {

    public HeatRay() {
        addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(new XValue()));
    }
}
