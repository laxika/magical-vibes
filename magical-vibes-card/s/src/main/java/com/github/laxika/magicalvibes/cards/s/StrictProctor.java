package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "STX", collectorNumber = "33")
public class StrictProctor extends Card {

    public StrictProctor() {
        addTrigger(Triggers.permanentEnteringCausesAbilityToTrigger(), new CounterUnlessPaysEffect(2));
    }
}
