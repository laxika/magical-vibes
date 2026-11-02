package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAttackedTargetEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RNA", collectorNumber = "198")
public class RakdosRoustabout extends Card {

    public RakdosRoustabout() {
        addTrigger(Triggers.becomesBlocked(), new DealDamageToAttackedTargetEffect(1));
    }
}
