package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.EventValueAtLeast;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "40K", collectorNumber = "124")
public class GhyrsonStarnKelermorph extends Card {

    public GhyrsonStarnKelermorph() {
        // Ward {2}
        addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(), new CounterUnlessPaysEffect(2));
        // Three Autostubs: "exactly 1 damage" is a fact of the event, judged as the ability triggers.
        addTrigger(Triggers.anotherSourceYouControlDealsDamageToPermanentOrPlayer(),
                TriggerOptions.DEFAULT.withEventCondition(new NotCondition(new EventValueAtLeast(2))),
                // "That permanent or player": the recipient the event names, not a target (CR 115.10a).
                DealDamageToAnyTargetEffect.toEventObject(new Fixed(2)));
    }
}
