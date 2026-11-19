package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.EventValueAtLeast;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "DIS", collectorNumber = "121")
public class PainMagnification extends Card {

    public PainMagnification() {
        addTrigger(Triggers.opponentDealtDamage(),
                TriggerOptions.DEFAULT.withEventCondition(new EventValueAtLeast(3)),
                new DiscardEffect(1, Subjects.targetPlayer()));
    }
}
