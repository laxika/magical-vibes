package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.SunbirdsInvocationRevealAndCastEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "XLN", collectorNumber = "165")
@CardRegistration(set = "C21", collectorNumber = "180")
@CardRegistration(set = "BLC", collectorNumber = "116")
@CardRegistration(set = "SCD", collectorNumber = "159")
public class SunbirdsInvocation extends Card {

    public SunbirdsInvocation() {
        addTrigger(Triggers.youCastSpell().castFrom(Zone.HAND), new SunbirdsInvocationRevealAndCastEffect());
    }
}
