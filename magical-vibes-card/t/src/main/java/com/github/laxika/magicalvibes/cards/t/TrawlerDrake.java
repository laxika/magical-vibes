package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DynamicStaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ONE", collectorNumber = "74")
public class TrawlerDrake extends Card {

    public TrawlerDrake() {
        addStaticEffect(new EnterWithCountersEffect(CounterType.OIL, new Fixed(1)));
        addStaticEffect(new DynamicStaticBoostEffect(new CountersOnSource(CounterType.OIL),
                        new CountersOnSource(CounterType.OIL), Subjects.source()));
        addTrigger(Triggers.youCastSpell().withFilter(new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))),
                PutCountersEffect.onSelf(CounterType.OIL));
    }
}
