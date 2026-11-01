package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.NoManaSpentToCast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "OTJ", collectorNumber = "230")
public class SatoruTheInfiltrator extends Card {

    public SatoruTheInfiltrator() {
        addTrigger(new TriggerCondition(Triggers.enters(Triggers.CREATURE, ObjectRelation.YOU_CONTROL)),
                TriggerOptions.ONE_OR_MORE,
                new TriggeringPermanentConditionalEffect(
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsSourcePermanentPredicate(),
                                new PermanentNotPredicate(new PermanentIsTokenPredicate())
                        )),
                        new ConditionalEffect(new NoManaSpentToCast(), new DrawCardEffect())
                ));
    }
}
