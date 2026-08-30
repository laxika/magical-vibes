package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AllConditions;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;

@CardRegistration(set = "MAT", collectorNumber = "11")
public class AyarasOathsworn extends Card {

    public AyarasOathsworn() {
        SourceCounterThreshold fourCounters = new SourceCounterThreshold(4, CounterType.PLUS_ONE_PLUS_ONE);
        SourceCounterThreshold fiveCounters = new SourceCounterThreshold(5, CounterType.PLUS_ONE_PLUS_ONE);

        // Whenever this creature deals combat damage to a player, if it has fewer than four +1/+1
        // counters on it, put a +1/+1 counter on it. Then if it has exactly four +1/+1 counters on
        // it, search your library for a card, put it into your hand, then shuffle.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new ConditionalEffect(
                new NotCondition(fourCounters),
                SequenceEffect.of(
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                        new ConditionalEffect(
                                new AllConditions(List.of(fourCounters, new NotCondition(fiveCounters))),
                                new SearchLibraryEffect()))));
    }
}
