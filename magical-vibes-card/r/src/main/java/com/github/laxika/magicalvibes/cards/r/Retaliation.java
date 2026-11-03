package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "USG", collectorNumber = "272")
public class Retaliation extends Card {

    public Retaliation() {
        addTrigger(Triggers.creatureYouControlBecomesBlocked(), TriggerOptions.EACH_BLOCKER, new BoostSelfEffect(1, 1));
    }
}
