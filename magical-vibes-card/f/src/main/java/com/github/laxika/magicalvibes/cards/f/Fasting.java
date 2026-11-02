package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.FastingEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DRK", collectorNumber = "7")
public class Fasting extends Card {

    public Fasting() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), SequenceEffect.of(
                new PutCountersOnSelfEffect(CounterType.HUNGER),
                new ConditionalEffect(new SourceCounterThreshold(5, CounterType.HUNGER),
                        new DestroyReferencedPermanentEffect(PermanentReference.SOURCE))));

        addStaticEffect(new FastingEffect());

        addTrigger(Triggers.youDrawCard(),
                new DestroyReferencedPermanentEffect(PermanentReference.SOURCE));
    }
}
