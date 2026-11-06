package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PoisonRecipient;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NPH", collectorNumber = "72")
public class ReaperOfSheoldred extends Card {

    public ReaperOfSheoldred() {
        // Whenever a source deals damage to this creature, that source's controller gets a poison counter.
        addTrigger(Triggers.dealtDamage(), TriggerOptions.EACH_SOURCE,
                new GivePoisonCountersEffect(1, PoisonRecipient.TRIGGERING_PERMANENT_CONTROLLER));
    }
}
