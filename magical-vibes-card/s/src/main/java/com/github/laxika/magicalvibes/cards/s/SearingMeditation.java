package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RAV", collectorNumber = "226")
@CardRegistration(set = "DDH", collectorNumber = "27")
public class SearingMeditation extends Card {

    public SearingMeditation() {
        addTrigger(Triggers.youGainLife(), new MayPayManaEffect(
                "{2}", DealDamageEffect.toAnyTarget(2),
                "Pay {2} to deal 2 damage to any target?"
        ));
    }
}
