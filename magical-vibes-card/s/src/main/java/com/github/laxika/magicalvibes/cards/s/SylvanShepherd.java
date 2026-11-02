package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.RollD20Effect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AFR", collectorNumber = "206")
@CardRegistration(set = "HBG", collectorNumber = "224")
public class SylvanShepherd extends Card {

    public SylvanShepherd() {
        addTrigger(Triggers.attacks(), new RollD20Effect(
                new GainLifeEffect(1),
                new GainLifeEffect(2),
                new GainLifeEffect(5)));
    }
}
