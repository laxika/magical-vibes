package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.FastingEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DRK", collectorNumber = "7")
public class Fasting extends Card {

    public Fasting() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), SequenceEffect.of(
                PutCountersEffect.onSelf(CounterType.HUNGER),
                new ConditionalEffect(new SourceCounterThreshold(5, CounterType.HUNGER),
                        new DestroyTargetPermanentEffect(Subjects.source()))));

        addStaticEffect(new FastingEffect());

        addTrigger(Triggers.youDrawCard(),
                new DestroyTargetPermanentEffect(Subjects.source()));
    }
}
