package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ONS", collectorNumber = "177")
public class ThrashingMudspawn extends Card {

    public ThrashingMudspawn() {
        addMorph("{1}{B}{B}");
        addTrigger(Triggers.dealtDamage(),
                new LoseLifeEffect(new EventValue(), LoseLifeRecipient.CONTROLLER));
    }
}
