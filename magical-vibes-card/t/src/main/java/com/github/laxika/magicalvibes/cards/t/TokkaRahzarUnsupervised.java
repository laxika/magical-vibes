package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TMC", collectorNumber = "28")
public class TokkaRahzarUnsupervised extends Card {

    public TokkaRahzarUnsupervised() {
        addTrigger(Triggers.leavesBattlefield(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.CREATURE,
                                new PermanentNotPredicate(new PermanentIsTokenPredicate()))),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN),
                SequenceEffect.of(
                                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                                CreateTokenEffect.ofTreasureToken(1)
                        ));
    }
}
