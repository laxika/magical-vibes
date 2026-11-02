package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAttackedTargetEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M20", collectorNumber = "159")
public class ScorchSpitter extends Card {

    public ScorchSpitter() {
        addTrigger(Triggers.attacks(), new DealDamageToAttackedTargetEffect(1));
    }
}
