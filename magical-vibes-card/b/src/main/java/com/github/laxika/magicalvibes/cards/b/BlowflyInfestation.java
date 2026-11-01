package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SHM", collectorNumber = "58")
@CardRegistration(set = "ECC", collectorNumber = "46")
@CardRegistration(set = "YECL", collectorNumber = "32")
public class BlowflyInfestation extends Card {

    public BlowflyInfestation() {
        // Whenever a creature dies, if it had a -1/-1 counter on it, put a -1/-1 counter on target creature.
        // The intervening-if is evaluated against the dying permanent's counters; the death-trigger target
        // pipeline restricts the target to creatures.
        addTrigger(Triggers.creatureDies(ObjectRelation.ANY), new TriggeringPermanentConditionalEffect(
                new PermanentHasCountersPredicate(CounterType.MINUS_ONE_MINUS_ONE),
                new PutCounterOnTargetPermanentEffect(CounterType.MINUS_ONE_MINUS_ONE, 1)));
    }
}
