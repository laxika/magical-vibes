package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.SpellXAtLeast;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "40K", collectorNumber = "149")
public class Zoanthrope extends Card {

    public Zoanthrope() {
        // Ravenous — this creature enters with X +1/+1 counters on it. If X is 5 or more,
        // draw a card when it enters.
        addStaticEffect(new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new XValue()));
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                TriggerMode.INDEPENDENT,
                TriggerOptions.DEFAULT.withInterveningIf(new SpellXAtLeast(5)),
                new DrawCardEffect(1));

        // Warp Blast — When this creature enters, it deals X damage to any target.
        addTrigger(Triggers.enters(ObjectRelation.THIS), TriggerMode.INDEPENDENT,
                new DealDamageToAnyTargetEffect(new XValue()));
    }
}
