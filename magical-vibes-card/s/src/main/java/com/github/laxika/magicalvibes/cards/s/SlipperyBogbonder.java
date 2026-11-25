package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.MoveCounterFromSourceToTargetCreatureEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NCC", collectorNumber = "312")
@CardRegistration(set = "C20", collectorNumber = "66")
public class SlipperyBogbonder extends Card {

    public SlipperyBogbonder() {
        target(TargetFilters.creature()).addTrigger(Triggers.enters(ObjectRelation.THIS), SequenceEffect.of(
                PutCountersEffect.withTargetRestriction(
                        CounterType.HEXPROOF, 1, new PermanentIsCreaturePredicate()),
                MoveCounterFromSourceToTargetCreatureEffect.anyNumber(CounterType.ANY, Subjects.creaturesYouControl(), Subjects.target())));
    }
}
