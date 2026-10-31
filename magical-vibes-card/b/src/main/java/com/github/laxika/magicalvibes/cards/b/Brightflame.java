package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetAndSharingCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "RAV", collectorNumber = "194")
@CardRegistration(set = "GK1", collectorNumber = "87")
public class Brightflame extends Card {

    public Brightflame() {
        target(TargetFilters.creature())
                .addSpellEffect(new DealDamageToTargetAndSharingCreaturesEffect(new XValue()))
                .addSpellEffect(new GainLifeEffect(new EventValue()));
    }
}
