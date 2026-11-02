package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YWOE", collectorNumber = "28")
public class ThirdLittlePig extends Card {

    public ThirdLittlePig() {
        addTrigger(Triggers.youConjure(), new PerpetuallyBoostSourceEffect(1, 1));
        addTrigger(new TriggerCondition(Triggers.youConjure(), AbilityZones.GRAVEYARD),
                TriggerOptions.DEFAULT, new PerpetuallyBoostSourceEffect(1, 1));
    }
}
