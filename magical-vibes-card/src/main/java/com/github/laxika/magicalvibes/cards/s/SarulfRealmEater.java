package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOtherNonlandPermanentsWithManaValueAtMostEventValueEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "KHM", collectorNumber = "228")
public class SarulfRealmEater extends Card {

    public SarulfRealmEater() {
        addEffect(EffectSlot.ON_OPPONENT_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));

        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ConditionalEffect(
                        new SourceCounterThreshold(1, CounterType.PLUS_ONE_PLUS_ONE),
                        new MayEffect(
                                SequenceEffect.of(
                                        new RemoveAllCountersEffect(CounterType.PLUS_ONE_PLUS_ONE),
                                        new ExileOtherNonlandPermanentsWithManaValueAtMostEventValueEffect()),
                                "Remove all +1/+1 counters from Sarulf, Realm Eater?")));
    }
}
