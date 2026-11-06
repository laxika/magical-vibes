package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NPH", collectorNumber = "68")
@CardRegistration(set = "ONE", collectorNumber = "105")
@CardRegistration(set = "A25", collectorNumber = "101")
@CardRegistration(set = "HA3", collectorNumber = "13")
public class PhyrexianObliterator extends Card {

    public PhyrexianObliterator() {
        // Whenever a source deals damage to this creature, that source's controller
        // sacrifices that many permanents.
        addTrigger(Triggers.dealtDamage(), TriggerOptions.EACH_SOURCE,
                new SacrificePermanentsEffect(new EventValue(), new PermanentTruePredicate(),
                        SacrificeRecipient.TRIGGERING_PERMANENT_CONTROLLER));
    }
}
