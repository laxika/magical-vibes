package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForFirstMatchingSpellEachTurnEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasXInManaCostPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOC", collectorNumber = "10")
public class ZimoneInfiniteAnalyst extends Card {

    public ZimoneInfiniteAnalyst() {
        addStaticEffect(new ReduceCastCostForFirstMatchingSpellEachTurnEffect(
                new CardHasXInManaCostPredicate(),
                new CountersOnSource(CounterType.PLUS_ONE_PLUS_ONE)));

        addTrigger(Triggers.youCastSpell().withFilter(new CardHasXInManaCostPredicate()).nthThisTurn(1),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 2));
    }
}
