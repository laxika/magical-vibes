package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TMT", collectorNumber = "30")
@CardRegistration(set = "TMT", collectorNumber = "206")
public class AprilReporterOfTheWeird extends Card {

    public AprilReporterOfTheWeird() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(), SequenceEffect.of(
                new DrawCardEffect(new EventValue()),
                new DiscardEffect(1, DiscardRecipient.CONTROLLER)));
    }
}
