package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DSK", collectorNumber = "54")
public class ErraticApparition extends Card {

    public ErraticApparition() {
        BoostSelfEffect boost = new BoostSelfEffect(1, 1);
        addTrigger(Triggers.enters(Triggers.ENCHANTMENT, ObjectRelation.ANOTHER_YOU_CONTROL), boost);
        addTrigger(Triggers.youFullyUnlockRoom(), boost);
    }
}
