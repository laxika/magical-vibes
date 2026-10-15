package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.AllCountersOnSource;
import com.github.laxika.magicalvibes.model.effect.DiscardAnyNumberThenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutKeywordCountersOnSelfForDiscardedCardsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "REX", collectorNumber = "14")
@CardRegistration(set = "REX", collectorNumber = "39")
public class IndominusRexAlpha extends Card {

    private static final List<CounterType> KEYWORD_COUNTERS = List.of(
            CounterType.FLYING,
            CounterType.FIRST_STRIKE,
            CounterType.DOUBLE_STRIKE,
            CounterType.DEATHTOUCH,
            CounterType.HEXPROOF,
            CounterType.HASTE,
            CounterType.INDESTRUCTIBLE,
            CounterType.LIFELINK,
            CounterType.MENACE,
            CounterType.REACH,
            CounterType.TRAMPLE,
            CounterType.VIGILANCE
    );

    public IndominusRexAlpha() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DiscardAnyNumberThenEffect(
                new CardTypePredicate(CardType.CREATURE),
                SequenceEffect.of(
                        new PutKeywordCountersOnSelfForDiscardedCardsEffect(KEYWORD_COUNTERS),
                        new DrawCardEffect(new AllCountersOnSource())),
                "creature cards",
                true));
    }
}
