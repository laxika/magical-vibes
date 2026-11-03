package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.ControlsAnotherPermanent;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LCI", collectorNumber = "208")
@CardRegistration(set = "LCI", collectorNumber = "328")
@CardRegistration(set = "FDC", collectorNumber = "220")
public class PugnaciousHammerskull extends Card {

    public PugnaciousHammerskull() {
        // "Whenever this creature attacks while you don't control another Dinosaur" is judged when the
        // attack is declared only; a Dinosaur gained in response does not stop the stun counter.
        addTrigger(Triggers.attacks(), TriggerOptions.DEFAULT.withEventCondition(
                new NotCondition(new ControlsAnotherPermanent(
                        new PermanentHasSubtypePredicate(CardSubtype.DINOSAUR)))),
                new PutCountersOnSelfEffect(CounterType.STUN));
    }
}
