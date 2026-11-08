package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.TriggeringPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentToughnessAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentToughnessAtMostPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ARB", collectorNumber = "77")
@CardRegistration(set = "CMD", collectorNumber = "225")
public class SigilCaptain extends Card {

    public SigilCaptain() {
        // Whenever a creature you control enters, if that creature is 1/1, put two +1/+1 counters on it. The
        // intervening "if" is judged as the creature enters and again as the ability resolves (CR 603.4).
        TriggeringPermanentMatches oneOne = new TriggeringPermanentMatches(new PermanentAllOfPredicate(List.of(
                new PermanentPowerAtLeastPredicate(1), new PermanentPowerAtMostPredicate(1),
                new PermanentToughnessAtLeastPredicate(1), new PermanentToughnessAtMostPredicate(1))));
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.YOU_CONTROL),
                TriggerOptions.DEFAULT.withInterveningIf(oneOne),
                new PutCounterOnReferencedPermanentEffect(PermanentReference.TRIGGERING,
                        CounterType.PLUS_ONE_PLUS_ONE, new Fixed(2)));
    }
}
