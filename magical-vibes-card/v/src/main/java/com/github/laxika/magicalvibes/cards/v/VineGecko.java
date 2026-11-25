package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForFirstMatchingSpellEachTurnEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ZNR", collectorNumber = "219")
public class VineGecko extends Card {

    public VineGecko() {
        addStaticEffect(new ReduceCastCostForFirstMatchingSpellEachTurnEffect(
                new CardTruePredicate(), 1, true));
        addTrigger(Triggers.youCastSpell().kicked(), PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
