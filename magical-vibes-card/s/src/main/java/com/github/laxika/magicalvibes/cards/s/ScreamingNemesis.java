package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyOtherTargetThenLockPlayerLifeGainEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DSK", collectorNumber = "157")
public class ScreamingNemesis extends Card {

    public ScreamingNemesis() {
        addTrigger(Triggers.dealtDamage(),
                new DealDamageToAnyOtherTargetThenLockPlayerLifeGainEffect(new EventValue()));
    }
}
