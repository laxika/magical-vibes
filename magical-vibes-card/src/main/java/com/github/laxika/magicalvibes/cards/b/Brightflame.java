package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetAndSharingCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "RAV", collectorNumber = "194")
public class Brightflame extends Card {

    public Brightflame() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new DealDamageToTargetAndSharingCreaturesEffect(new XValue()))
                .addEffect(EffectSlot.SPELL, new GainLifeEffect(new EventValue()));
    }
}
