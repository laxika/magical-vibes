package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetForEachDyingSourceCounterEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAtLeastCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "C16", collectorNumber = "40")
@CardRegistration(set = "CM2", collectorNumber = "13")
public class ReyhanLastOfTheAbzan extends Card {

    public ReyhanLastOfTheAbzan() {
        PermanentPredicate hadCounters = new PermanentHasAtLeastCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE, 1);
        CardEffect counterTransfer = new MayEffect(
                new PutCounterOnTargetForEachDyingSourceCounterEffect(CounterType.PLUS_ONE_PLUS_ONE),
                "Put that many +1/+1 counters on target creature?");

        addTrigger(Triggers.creatureDies(hadCounters, ObjectRelation.YOU_CONTROL), counterTransfer);
        addTrigger(Triggers.putIntoCommandZoneFrom(Zone.BATTLEFIELD,
                new PermanentAllOfPredicate(List.of(Triggers.CREATURE, hadCounters)), ObjectRelation.YOU_CONTROL),
                counterTransfer);
    }
}
