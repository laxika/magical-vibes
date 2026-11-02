package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M12", collectorNumber = "10")
public class BenalishVeteran extends Card {

    public BenalishVeteran() {
        addTrigger(Triggers.attacks(), new BoostSelfEffect(1, 1));
    }
}
