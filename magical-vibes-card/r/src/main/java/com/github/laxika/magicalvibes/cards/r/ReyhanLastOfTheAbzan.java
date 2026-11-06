package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetForEachDyingSourceCounterEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAtLeastCountersPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "C16", collectorNumber = "40")
@CardRegistration(set = "CM2", collectorNumber = "13")
public class ReyhanLastOfTheAbzan extends Card {

    public ReyhanLastOfTheAbzan() {
        CardEffect counterTransfer = new MayEffect(
                        new PutCounterOnTargetForEachDyingSourceCounterEffect(CounterType.PLUS_ONE_PLUS_ONE),
                        "Put that many +1/+1 counters on target creature?");

        addTrigger(Triggers.creatureDies(
                        new PermanentHasAtLeastCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE, 1),
                        ObjectRelation.YOU_CONTROL),
                counterTransfer);
        addTrigger(Triggers.putIntoCommandZone(
                        new PermanentHasAtLeastCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE, 1),
                        ObjectRelation.YOU_OWN),
                new MayEffect(
                        new PutCounterOnTargetForEachDyingSourceCounterEffect(CounterType.PLUS_ONE_PLUS_ONE),
                        "Put that many +1/+1 counters on target creature?"));
    }
}
