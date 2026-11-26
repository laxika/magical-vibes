package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BOK", collectorNumber = "117")
public class ShinkaGatekeeper extends Card {

    public ShinkaGatekeeper() {
        addTrigger(Triggers.dealtDamage(),
                DealDamageEffect.to(Subjects.you(), new EventValue()));
    }
}
