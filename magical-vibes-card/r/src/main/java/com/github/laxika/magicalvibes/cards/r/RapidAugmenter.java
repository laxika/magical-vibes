package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentBasePowerEqualsPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentWasCastPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BLC", collectorNumber = "38")
@CardRegistration(set = "BLC", collectorNumber = "70")
public class RapidAugmenter extends Card {

    public RapidAugmenter() {
        // Whenever another creature you control with base power 1 enters, it gains haste until end of turn.
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.CREATURE,
                                new PermanentBasePowerEqualsPredicate(1))),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                new BoostEnteringCreatureEffect(0, 0, Set.of(Keyword.HASTE)));

        // Whenever another creature you control enters, if it wasn't cast, put a +1/+1 counter on
        // this creature and this creature can't be blocked this turn.
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.CREATURE,
                                new PermanentNotPredicate(new PermanentWasCastPredicate()))),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                SequenceEffect.of(
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                        new MakeCreatureUnblockableEffect(true)));
    }
}
